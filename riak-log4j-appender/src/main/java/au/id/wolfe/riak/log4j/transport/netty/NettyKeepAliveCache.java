/**
 *
 * Copyright (C) 2010 markw <mark@wolfe.id.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.id.wolfe.riak.log4j.transport.netty;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static au.id.wolfe.riak.log4j.utils.Tracer.log;

/**
 * This class manages reuse of netty transport handlers, and their underlying http connections.
 */
public class NettyKeepAliveCache implements Runnable {

    /* This is the number of connections which will be retained for a given URL */
    public static final int MAX_CONNECTIONS = 5;

    /* This is the value in seconds for how long connections live */
    public static final int LIFETIME = 5;

    private Hashtable<KeepAliveKey, TransportHandlerVector> keepAliveTable =
            new Hashtable<KeepAliveKey, TransportHandlerVector>();

    private ScheduledExecutorService keepAliveExecutorService = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> keepAliveFuture = null;

    public synchronized void put(final URL url, NettyTransportHandler nettyTransportHandler) {

        boolean startExecutor = (keepAliveFuture == null || keepAliveFuture.isCancelled());

        log(getClass().getSimpleName() + " " + keepAliveFuture);

        if (startExecutor) {
            keepAliveFuture = keepAliveExecutorService.scheduleAtFixedRate(this, LIFETIME, LIFETIME, TimeUnit.SECONDS);
        }

        KeepAliveKey keepAliveKey = new KeepAliveKey(url);

        TransportHandlerVector transportHandlerVector = keepAliveTable.get(keepAliveKey);

        if (transportHandlerVector == null) {
            int keepAliveMilliseconds = (LIFETIME * 1000);
            transportHandlerVector = new TransportHandlerVector(keepAliveMilliseconds);
            transportHandlerVector.put(nettyTransportHandler);
            keepAliveTable.put(keepAliveKey, transportHandlerVector);
        } else {
            if (transportHandlerVector.size() < MAX_CONNECTIONS) {
                transportHandlerVector.put(nettyTransportHandler);
            }
        }

    }

    public synchronized NettyTransportHandler get(URL url) {
        KeepAliveKey keepAliveKey = new KeepAliveKey(url);
        TransportHandlerVector transportHandlerVector = keepAliveTable.get(keepAliveKey);
        if (transportHandlerVector == null) {
//          System.out.println("no transport - " + url);
            return null;
        } else {
            return transportHandlerVector.get();
        }
    }

    public void run() {

        log(getClass().getSimpleName() + " - run");

        synchronized (this) {

            long currentTime = System.currentTimeMillis();

            List<KeepAliveKey> keysToRemove = new ArrayList<KeepAliveKey>();

            for (KeepAliveKey keepAliveKey : keepAliveTable.keySet()) {
                TransportHandlerVector transportHandlerVector = keepAliveTable.get(keepAliveKey);

                int i;

                for (i = 0; i < transportHandlerVector.size(); i++) {
                    KeepAliveEntry keepAliveEntry = transportHandlerVector.elementAt(i);
                    if ((currentTime - keepAliveEntry.idleStartTime) > transportHandlerVector.sleepTimeInMilliseconds) {
                        //System.out.println("connection end - " + keepAliveEntry.nettyTransportHandler.getUrl());
                        keepAliveEntry.nettyTransportHandler.end();
                    } else {
                        break;
                    }
                }

                // This is an amazing little trick, using the FIFO and count to calculate how many to clear
                // from the underlying collection.
                transportHandlerVector.subList(0, i).clear();

                if (transportHandlerVector.size() == 0) {
                    keysToRemove.add(keepAliveKey);
                }


            } // end for

            for (KeepAliveKey keepAliveKeyRem : keysToRemove) {
                removeVector(keepAliveKeyRem);
            }

        } // end sync


    }

    synchronized void removeVector(KeepAliveKey keepAliveKey) {
        keepAliveTable.remove(keepAliveKey);
    }

    class TransportHandlerVector extends Stack<KeepAliveEntry> {

        int sleepTimeInMilliseconds;

        TransportHandlerVector(int sleepTimeInMilliseconds) {
            this.sleepTimeInMilliseconds = sleepTimeInMilliseconds;
        }

        synchronized NettyTransportHandler get() {

            // simple no entries short circuit routine
            if (empty()) {
                return null;
            } else {

                // loop until we find a connection which hasn't
                NettyTransportHandler nettyTransportHandler = null;
                long currentTime = System.currentTimeMillis();
                do {

                    KeepAliveEntry keepAliveEntry = pop();

                    if ((currentTime - keepAliveEntry.idleStartTime) > sleepTimeInMilliseconds) {
//                      System.out.println("Idle time = " +
//                          (currentTime - keepAliveEntry.idleStartTime) + ", " + keepAliveTable.size());
                        keepAliveEntry.nettyTransportHandler.end();
                    } else {
                        nettyTransportHandler = keepAliveEntry.nettyTransportHandler;
                    }

                } while (nettyTransportHandler == null && !empty());

                return nettyTransportHandler;
            }

        }

        synchronized void put(NettyTransportHandler nettyTransportHandler) {
            if (size() > MAX_CONNECTIONS) {
//                System.out.println("Max connections hit");
                nettyTransportHandler.end();
            } else {
                push(new KeepAliveEntry(nettyTransportHandler, System.currentTimeMillis()));
            }
        }


    }


    class KeepAliveKey {
        private String protocol = null;
        private String host = null;
        private int port = 0;

        KeepAliveKey(URL url) {
            this.protocol = url.getProtocol();
            this.host = url.getHost();
            this.port = url.getPort();
        }

        @Override
        public int hashCode() {
            return new StringBuilder().append(protocol).append(host).append(port).toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeepAliveKey)) {
                return false;
            }
            KeepAliveKey keepAliveKey = (KeepAliveKey) obj;
            return host.equals(keepAliveKey.host)
                    && (port == keepAliveKey.port)
                    && protocol.equals(keepAliveKey.protocol);
        }
    }

    class KeepAliveEntry {
        NettyTransportHandler nettyTransportHandler;
        long idleStartTime;

        KeepAliveEntry(NettyTransportHandler nettyTransportHandler, long idleStartTime) {
            this.nettyTransportHandler = nettyTransportHandler;
            this.idleStartTime = idleStartTime;
        }
    }

    public synchronized void shutdown() {

        keepAliveExecutorService.shutdownNow();

        for (TransportHandlerVector transportHandlerVector : keepAliveTable.values()) {

            Enumeration<KeepAliveEntry> elements =  transportHandlerVector.elements();

            while (elements.hasMoreElements()) {
                KeepAliveEntry keepAliveEntry = elements.nextElement();
                keepAliveEntry.nettyTransportHandler.end();
            }
        }


        keepAliveTable.clear();
    }
}

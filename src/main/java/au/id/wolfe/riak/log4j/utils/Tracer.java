package au.id.wolfe.riak.log4j.utils;

/**
 * Just used for trace message.
 */
public final class Tracer {

    private static boolean traceEnabled = false;

    public static void log(String message){

        if (traceEnabled){
            System.out.println(message);
        }
    }

    public static void setTraceEnabled(boolean traceEnabled) {
        Tracer.traceEnabled = traceEnabled;
    }
}

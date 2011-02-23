package au.id.wolfe.riak.log4j.example;

import au.id.wolfe.riak.log4j.example.data.Person;
import org.apache.cxf.feature.Features;

import javax.jws.WebService;

/**
 * Implementation of the person service.
 */
@WebService(endpointInterface = "au.id.wolfe.riak.log4j.example.ContactPersonService")
@Features(features = "org.apache.cxf.feature.LoggingFeature")
public class ContactPersonServiceImpl implements ContactPersonService {

    @Override
    public Person getContactPersonByName(String firstName, String lastName) {
        return new Person(firstName, "Fred", lastName, "me@here.com");
    }
}

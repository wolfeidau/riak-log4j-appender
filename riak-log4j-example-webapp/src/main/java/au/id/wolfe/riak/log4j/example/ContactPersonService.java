package au.id.wolfe.riak.log4j.example;

import au.id.wolfe.riak.log4j.example.data.Person;

import javax.jws.WebService;

/**
 * Example contact person service
 */
@WebService
public interface ContactPersonService {

    Person getContactPersonByName(String firstName, String lastName);
}

package au.id.wolfe.riak.log4j.example.data;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Contact Person
 */
public class Person {

    private String firstName;
    private String middleName;
    private String lastName;
    private String emailAddress;

    public Person() {
    }

    public Person(String firstName, String middleName, String lastName, String emailAddress) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("firstName", firstName).
                append("middleName", middleName).
                append("lastName", lastName).
                append("emailAddress", emailAddress).
                toString();
    }
}

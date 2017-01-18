package com.android.shnellers.heartrate;

/**
 * Created by Sean on 24/10/2016.
 */

public class User {

    private String _name;
    private String _age;
    private String _email;
    private String _condition;



    private String _weight;
    private String _location;
    private String _phoneNumber;

    public User(final String name,
                 final String age,
                 final String email,
                 final String condition,
                 final String weight,
                 final String location,
                 final String phoneNumber) {
        set_name(name);
        set_age(age);
        set_email(email);
        set_condition(condition);
        set_location(location);
        set_weight(weight);
        set_phoneNumber(phoneNumber);

    }

    public User(final String name,
                final String email) {
        set_name(name);
        set_email(email);

    }

    /**
     * Get the users name.
     *
     * @return
     */
    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    /**
     * Get the users condition.
     *
     * @return
     */
    public String get_condition() {
        return _condition;
    }

    public void set_condition(String _condition) {
        this._condition = _condition;
    }

    /**
     * Get the users email.
     *
     * @return
     */
    public String get_email() {
        return _email;
    }

    public void set_email(String _email) {
        this._email = _email;
    }

    /**
     * Get the age of the user.
     *
     * @return
     */
    public String get_age() {
        return _age;
    }

    public void set_age(String _age) {
        this._age = _age;
    }

    /**
     * Get the weight of the user.
     *
     * @return
     */
    public String get_weight() {
        return _weight;
    }

    public void set_weight(String _weight) {
        this._weight = _weight;
    }

    /**
     * Get the location of the user.
     *
     * @return
     */
    public String get_location() {
        return _location;
    }

    /**
     * Set the location of the user.
     *
     * @param _location
     */
    public void set_location(String _location) {
        this._location = _location;
    }

    /**
     * Get the users phone number.
     *
     * @return
     */
    public String get_phoneNumber() {
        return _phoneNumber;
    }

    /**
     * Set the weight of the user.
     *
     * @param _phoneNumber
     */
    public void set_phoneNumber(String _phoneNumber) {
        this._phoneNumber = _phoneNumber;
    }
}

package com.mau.dalvi.malincompass;

/**
 * Created by Kristofer Svensson on 2017-02-21.
 */

/**
 * A class representing a user of the application.
 */
public class User {

    private String _name, _password;
    private int _steps;

    public User(String _name, String _password, int _steps) {
        this._name = _name;
        this._password = _password;
        this._steps = _steps;
    }

    public User(int _steps) {
        this._steps = _steps;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_password() {
        return _password;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public int get_steps() {
        return _steps;
    }

    public void set_steps(int _steps) {
        this._steps = _steps;
    }
}

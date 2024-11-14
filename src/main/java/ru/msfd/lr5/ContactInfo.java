package ru.msfd.lr5;

public class ContactInfo
{
    public ContactInfo(int id, String name, String fullName, String email, String homePhone, String mobilePhone)
    {
        this.id = id;
        this.name = name;
        this.fullName = name;
        this.email = email;
        this.homePhone = homePhone;
        this.mobilePhone = mobilePhone;
    }

    public int id;
    public String name;
    public String fullName;
    public String email;
    public String homePhone;
    public String mobilePhone;
}

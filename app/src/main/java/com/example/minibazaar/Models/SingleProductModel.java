package com.example.minibazaar.Models;

import java.io.Serializable;

public class SingleProductModel implements Serializable {

    private int prid,no_of_items;
    private String useremail,usermobile,prname,prprice,primage,prdesc,message_header,message_body;

    public SingleProductModel() {
    }

    public SingleProductModel(int prid, int no_of_items, String useremail, String usermobile, String prname, String prprice, String primage, String prdesc, String message_header, String message_body) {
        this.prid = prid;
        this.no_of_items = no_of_items;
        this.useremail = useremail;
        this.usermobile = usermobile;
        this.prname = prname;
        this.prprice = prprice;
        this.primage = primage;
        this.prdesc = prdesc;
        this.message_header = message_header;
        this.message_body = message_body;
    }

    public String getMessage_header() {
        return message_header;
    }

    public void setMessage_header(String message_header) {
        this.message_header = message_header;
    }

    public String getMessage_body() {
        return message_body;
    }

    public void setMessage_body(String message_body) {
        this.message_body = message_body;
    }

    public String getUsermobile() {
        return usermobile;
    }

    public void setUsermobile(String usermobile) {
        this.usermobile = usermobile;
    }

    public int getNo_of_items() {
        return no_of_items;
    }

    public void setNo_of_items(int no_of_items) {
        this.no_of_items = no_of_items;
    }

    public String getPrdesc() {
        return prdesc;
    }

    public void setPrdesc(String prdesc) {
        this.prdesc = prdesc;
    }

    public int getPrid() {
        return prid;
    }

    public void setPrid(int prid) {
        this.prid = prid;
    }

    public String getUseremail() {
        return useremail;
    }

    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }

    public String getPrname() {
        return prname;
    }

    public void setPrname(String prname) {
        this.prname = prname;
    }

    public String getPrprice() {
        return prprice;
    }

    public void setPrprice(String prprice) {
        this.prprice = prprice;
    }

    public String getPrimage() {
        return primage;
    }

    public void setPrimage(String primage) {
        this.primage = primage;
    }
}

package com.kroposki.sftpapp;

public interface Constants {

    //Web Server URLs for Login Authentication, removed for Git
    String loginURL = "";
    String registerURL = "";

    //Removed for Git
    /**
     * PLEASE READ, IMPORTANT*************************************************************************************
     * The SFTP credentials shouldn't ever be stored in this manor, and the approach I would typically take
     * to do this would involve Asymmetrically storing the SFTP credentials and accessing them again, however
     * for the purposes of this assignment, I felt that I displayed my ability to implement credential authentication
     * and encryption through the use of the login/register system, and have decided to place the credentials in this
     * manor.
     */
    String SFTP_HOST = "";
    String SFTP_PASS = "";
    int SFTP_PORT = 22;
}

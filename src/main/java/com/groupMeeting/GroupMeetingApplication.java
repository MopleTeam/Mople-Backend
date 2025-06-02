package com.groupMeeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@SpringBootApplication
public class GroupMeetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupMeetingApplication.class, args);
    }
}

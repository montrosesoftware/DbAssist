package com.montrosesoftware;

import java.util.List;

public class Demo {
    public static void main(String[] args){
        System.out.println("Two sources:");

        try ( HibernateManager hibernateManager = new HibernateManager()) {

            List<User> usersFromHibernate = hibernateManager.getData();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Finished");
    }
}

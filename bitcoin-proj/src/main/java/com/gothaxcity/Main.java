package com.gothaxcity;

import com.gothaxcity.service.FullNode;
import com.gothaxcity.service.QueryProcess;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        FullNode fullNode = new FullNode();
        Thread fullNodeThread = new Thread(fullNode);
        fullNodeThread.start();

        QueryProcess queryProcess = new QueryProcess();
        Thread queryProcessThread = new Thread(queryProcess);
        queryProcessThread.setDaemon(true);
        queryProcessThread.start();
    }
}
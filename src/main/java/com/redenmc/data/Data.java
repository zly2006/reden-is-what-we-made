package com.redenmc.data;

import net.fabricmc.api.ClientModInitializer;

import java.util.Scanner;

public class Data implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Data generator launched");
        String command = scanner.nextLine();
        switch (command) {

        }
    }
}

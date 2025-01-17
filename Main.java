package com.carrental;

import java.util.Scanner;
// Main class
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CarRentalSystem system = new CarRentalSystem();

        while (true) {
            System.out.println("\n===== Car Rental System =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter your name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter your password: ");
                    String password = scanner.nextLine();
                    System.out.print("Enter your email: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter your phone number: ");
                    String phoneNumber = scanner.nextLine();
                    System.out.print("Enter your address: ");
                    String address = scanner.nextLine();

                    String customerId = system.registerCustomer(name, password, email, phoneNumber, address);
                    if (customerId != null) {
                        System.out.println("Customer registered successfully!");
                        System.out.println("Your Customer ID: " + customerId);
                    } else {
                        System.out.println("Error during registration. Please try again.");
                    }
                }
                case 2 -> {
                    System.out.print("Enter your Customer ID: ");
                    String customerId = scanner.nextLine();
                    System.out.print("Enter your password: ");
                    String password = scanner.nextLine();
                    String role = system.loginCustomer(customerId, password);
                    if (role != null) {
                        if (role.equalsIgnoreCase("Admin")) {
                            showAdminMenu(scanner, system); // Admin Menu
                        } else {
                            showCustomerMenu(scanner, system, customerId); // Customer Menu
                        }
                    } else {
                        System.out.println("Login failed. Please try again.");
                    }
                }
                case 3 -> {
                    System.out.println("Exiting the system. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void showCustomerMenu(Scanner scanner, CarRentalSystem system, String customerId) {
        while (true) {
            System.out.println("\n===== Customer Menu =====");
            System.out.println("1. Rent a Car");
            System.out.println("2. Return a Car");
            System.out.println("3. Display Available Cars");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> {
                    system.displayAvailableCars();
                    System.out.print("Enter Car ID to Rent: ");
                    String carId = scanner.nextLine();
                    System.out.print("Enter Rental Days: ");
                    int days = scanner.nextInt();
                    system.rentCar(carId, customerId, days);
                }
                case 2 -> {
                    System.out.print("Enter Car ID: ");
                    String carId = scanner.nextLine();
                    system.returnCar(carId, customerId);
                }
                case 3 -> system.displayAvailableCars();
                case 4 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void showAdminMenu(Scanner scanner, CarRentalSystem system) {
        while (true) {
            System.out.println("\n===== Admin Menu =====");
            System.out.println("1. View All Rental History");
            System.out.println("2. Add a Car");
            System.out.println("3. Update Car Details");
            System.out.println("4. Delete a Car");
            System.out.println("5. Display Available Cars");
            System.out.println("6. Generate Earnings Report");
            System.out.println("7. Logout");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> system.displayAllRentalHistory();
                case 2 -> {
                    System.out.print("Enter Car ID: ");
                    String carId = scanner.nextLine();
                    System.out.print("Enter Brand: ");
                    String brand = scanner.nextLine();
                    System.out.print("Enter Model: ");
                    String model = scanner.nextLine();
                    System.out.print("Enter Base Price Per Day: ");
                    double price = scanner.nextDouble();
                    scanner.nextLine(); // Consume newline
                    system.addCar(carId, brand, model, price);
                }
                case 3 -> {
                    System.out.print("Enter Car ID to Update: ");
                    String carId = scanner.nextLine();
                    System.out.print("Enter New Brand: ");
                    String brand = scanner.nextLine();
                    System.out.print("Enter New Model: ");
                    String model = scanner.nextLine();
                    System.out.print("Enter New Price Per Day: ");
                    double price = scanner.nextDouble();
                    scanner.nextLine(); // Consume newline
                    system.updateCar(carId, brand, model, price);
                }
                case 4 -> {
                    System.out.print("Enter Car ID to Delete: ");
                    String carId = scanner.nextLine();
                    system.deleteCar(carId);
                }
                case 5 -> system.displayAvailableCars(); // Admin can now view available cars
                case 6 -> system.generateEarningsReport();
                case 7 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }
}


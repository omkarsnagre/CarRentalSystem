package com.carrental;

import java.sql.*;
import java.util.Scanner;

public class CarRentalSystem {
    private Connection connection;

    public CarRentalSystem() {
        try {
            connection = DatabaseConnection.getConnection();
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }

    // Register a new customer
    public String registerCustomer(String name, String password, String email, String phoneNumber, String address) {
        String customerId = generateCustomerId();
        String query = "INSERT INTO customers (customer_id, name, password, email, phone_number, address, role) VALUES (?, ?, ?, ?, ?, ?, 'Customer')";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, customerId);
            statement.setString(2, name);
            statement.setString(3, password);
            statement.setString(4, email);
            statement.setString(5, phoneNumber);
            statement.setString(6, address);
            statement.executeUpdate();
            return customerId;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String loginCustomer(String customerId, String password) {
        String query = "SELECT * FROM customers WHERE customer_id = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, customerId);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                System.out.println("Login successful! Welcome, " + resultSet.getString("name"));
                return resultSet.getString("role");
            } else {
                System.out.println("Invalid Customer ID or Password.");
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Error during login.");
            e.printStackTrace();
            return null;
        }
    }

    private String generateCustomerId() {
        String query = "SELECT COUNT(*) AS count FROM customers";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                int count = resultSet.getInt("count") + 1;
                return "CUS" + String.format("%04d", count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "CUS0001";
    }

    public void rentCar(String carId, String customerId, int rentalDays) {
        String checkAvailabilityQuery = "SELECT * FROM rentals WHERE car_id = ? AND status = 'On Rent'";
        String getPriceQuery = "SELECT base_price_per_day FROM cars WHERE car_id = ? AND is_available = TRUE";
        String rentQuery = "INSERT INTO rentals (car_id, customer_id, rental_days, status, rented_at, payment_status) VALUES (?, ?, ?, 'On Rent', CURRENT_TIMESTAMP, 'Pending')";
        String updateCarQuery = "UPDATE cars SET is_available = FALSE WHERE car_id = ?";
        String paymentQuery = "INSERT INTO payments (rental_id, customer_id, amount_paid, payment_method) VALUES (?, ?, ?, ?)";

        try (PreparedStatement checkStatement = connection.prepareStatement(checkAvailabilityQuery);
             PreparedStatement priceStatement = connection.prepareStatement(getPriceQuery);
             PreparedStatement rentStatement = connection.prepareStatement(rentQuery, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement updateCarStatement = connection.prepareStatement(updateCarQuery);
             PreparedStatement paymentStatement = connection.prepareStatement(paymentQuery)) {

            checkStatement.setString(1, carId);
            ResultSet availabilityResult = checkStatement.executeQuery();
            if (availabilityResult.next()) {
                System.out.println("Error: This car is already rented. Please choose another car.");
                return;
            }

            priceStatement.setString(1, carId);
            ResultSet resultSet = priceStatement.executeQuery();
            if (resultSet.next()) {
                double pricePerDay = resultSet.getDouble("base_price_per_day");
                double totalCost = pricePerDay * rentalDays;
                System.out.printf("Total cost: ₹%.2f%n", totalCost);

                rentStatement.setString(1, carId);
                rentStatement.setString(2, customerId);
                rentStatement.setInt(3, rentalDays);
                rentStatement.executeUpdate();

                ResultSet generatedKeys = rentStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int rentalId = generatedKeys.getInt(1);

                    System.out.print("Enter payment method (e.g., Credit Card, Cash, UPI): ");
                    Scanner scanner = new Scanner(System.in);
                    String paymentMethod = scanner.nextLine();

                    paymentStatement.setInt(1, rentalId);
                    paymentStatement.setString(2, customerId);
                    paymentStatement.setDouble(3, totalCost);
                    paymentStatement.setString(4, paymentMethod);
                    paymentStatement.executeUpdate();

                    String updatePaymentStatusQuery = "UPDATE rentals SET payment_status = 'Completed' WHERE rental_id = ?";
                    try (PreparedStatement updatePaymentStatusStatement = connection.prepareStatement(updatePaymentStatusQuery)) {
                        updatePaymentStatusStatement.setInt(1, rentalId);
                        updatePaymentStatusStatement.executeUpdate();
                    }

                    updateCarStatement.setString(1, carId);
                    updateCarStatement.executeUpdate();

                    System.out.println("Car rented and payment processed successfully!");
                }
            } else {
                System.out.println("Car is not available or does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("Error while processing the rental or payment.");
            e.printStackTrace();
        }
    }

    public void returnCar(String carId, String customerId) {
        String checkRentalQuery = "SELECT rental_id FROM rentals WHERE car_id = ? AND customer_id = ? AND status = 'On Rent'";
        String updateRentalStatusQuery = "UPDATE rentals SET status = 'Returned', returned_at = CURRENT_TIMESTAMP WHERE rental_id = ?";
        String updateCarAvailabilityQuery = "UPDATE cars SET is_available = TRUE WHERE car_id = ?";

        try (PreparedStatement checkStatement = connection.prepareStatement(checkRentalQuery);
             PreparedStatement updateRentalStatusStatement = connection.prepareStatement(updateRentalStatusQuery);
             PreparedStatement updateCarAvailabilityStatement = connection.prepareStatement(updateCarAvailabilityQuery)) {

            checkStatement.setString(1, carId);
            checkStatement.setString(2, customerId);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                int rentalId = resultSet.getInt("rental_id");

                updateRentalStatusStatement.setInt(1, rentalId);
                updateRentalStatusStatement.executeUpdate();

                updateCarAvailabilityStatement.setString(1, carId);
                updateCarAvailabilityStatement.executeUpdate();

                System.out.println("Car returned successfully!");
            } else {
                System.out.println("You are not authorized to return this car, or it is not currently rented.");
            }
        } catch (SQLException e) {
            System.out.println("Error while returning the car.");
            e.printStackTrace();
        }
    }

    public void displayAvailableCars() {
        String query = "SELECT * FROM cars WHERE is_available = TRUE";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            System.out.println("Available Cars:");
            System.out.println("Car ID  | Brand    | Model      | Price Per Day");
            while (resultSet.next()) {
                System.out.printf("%-8s | %-10s | %-10s | ₹%.2f%n",
                        resultSet.getString("car_id"),
                        resultSet.getString("brand"),
                        resultSet.getString("model"),
                        resultSet.getDouble("base_price_per_day"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving available cars.");
            e.printStackTrace();
        }
    }

    public void addCar(String carId, String brand, String model, double price) {
        String query = "INSERT INTO cars (car_id, brand, model, base_price_per_day, is_available) VALUES (?, ?, ?, ?, TRUE)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, carId);
            statement.setString(2, brand);
            statement.setString(3, model);
            statement.setDouble(4, price);
            statement.executeUpdate();
            System.out.println("Car added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding car.");
            e.printStackTrace();
        }
    }

    public void updateCar(String carId, String brand, String model, double price) {
        String query = "UPDATE cars SET brand = ?, model = ?, base_price_per_day = ? WHERE car_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, brand);
            statement.setString(2, model);
            statement.setDouble(3, price);
            statement.setString(4, carId);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Car updated successfully!");
            } else {
                System.out.println("Car not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating car.");
            e.printStackTrace();
        }
    }

    public void deleteCar(String carId) {
        String query = "DELETE FROM cars WHERE car_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, carId);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Car deleted successfully!");
            } else {
                System.out.println("Car not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting car.");
            e.printStackTrace();
        }
    }

    public void generateEarningsReport() {
        String query = "SELECT c.car_id, c.brand, c.model, SUM(r.rental_days * c.base_price_per_day) AS total_earnings " +
                "FROM rentals r " +
                "INNER JOIN cars c ON r.car_id = c.car_id " +
                "WHERE r.status = 'Returned' " +
                "GROUP BY c.car_id, c.brand, c.model";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Earnings Report:");
            System.out.println("Car ID  | Brand    | Model      | Total Earnings");
            while (resultSet.next()) {
                System.out.printf("%-8s | %-8s | %-10s | ₹%.2f%n",
                        resultSet.getString("car_id"),
                        resultSet.getString("brand"),
                        resultSet.getString("model"),
                        resultSet.getDouble("total_earnings"));
            }
        } catch (SQLException e) {
            System.out.println("Error generating earnings report.");
            e.printStackTrace();
        }
    }

    public void displayAllRentalHistory() {
        String query = "SELECT r.rental_id, r.car_id, r.customer_id, r.rental_days, r.status, r.rented_at, r.returned_at, r.payment_status, " +
                "c.brand, c.model, cu.name AS customer_name " +
                "FROM rentals r " +
                "INNER JOIN cars c ON r.car_id = c.car_id " +
                "INNER JOIN customers cu ON r.customer_id = cu.customer_id";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("\nAll Rental History:");
            System.out.println("Rental ID | Car ID  | Customer ID | Customer Name | Brand      | Model      | Rental Days | Status   | Payment Status | Rented At          | Returned At");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");

            while (resultSet.next()) {
                System.out.printf("%-10d | %-7s | %-12s | %-13s | %-10s | %-10s | %-12d | %-8s | %-14s | %-19s | %-19s%n",
                        resultSet.getInt("rental_id"),
                        resultSet.getString("car_id"),
                        resultSet.getString("customer_id"),
                        resultSet.getString("customer_name"),
                        resultSet.getString("brand"),
                        resultSet.getString("model"),
                        resultSet.getInt("rental_days"),
                        resultSet.getString("status"),
                        resultSet.getString("payment_status"),
                        resultSet.getTimestamp("rented_at"),
                        resultSet.getTimestamp("returned_at"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving rental history.");
            e.printStackTrace();
        }
    }
}

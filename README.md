# Car Rental System
A Java-based Car Rental System designed using Object-Oriented Programming (OOP) principles, featuring role-based access for admins and customers. The system integrates MySQL for managing car inventory, rental transactions, and payments.

## Features
### Admin Features
- Add, update, delete cars from inventory.
- View rental history and generate earnings reports.
- Display available cars in real-time.

### Customer Features
- Browse available cars and rent with multiple payment options (Credit Card, UPI, Cash).
- Return cars and track rental history.

### System Highlights
- Role-based access control for security.
- Real-time CRUD operations and dynamic payment status updates.
- Scalable, modular, and optimized database queries.

## Technologies Used
- **Programming Language:** Java  
- **IDE:** IntelliJ IDEA  
- **Database:** MySQL  
- **Database Connectivity:** JDBC  
- **Version Control:** Git, GitHub  

## How to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/<omkarsnagre>/CarRentalSystem.git

2. Configure the database:
- Create a MySQL database named car_rental.
- Import the provided car_rental.sql script.
- Update DatabaseConnection.java with your MySQL credentials
   ```bash
   private static final String URL = "jdbc:mysql://localhost:3306/car_rental";
   private static final String USER = "your_username";
   private static final String PASSWORD = "your_password";

3. Build and run the project in IntelliJ IDEA.

## Contact 📫
Got questions or suggestions? I’d love to hear from you! You can reach me at omkarnagre777@gmail.com .



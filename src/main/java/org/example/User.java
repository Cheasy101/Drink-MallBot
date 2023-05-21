package org.example;
import java.sql.*;

public class User {
    public void addUserInUsersDatabase(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
//            Class.forName("org.sqlite.JDBC");
            conn = DatabaseHandler.getConnection();

            // Проверяем, есть ли такой пользователь в базе данных
            pstmt = conn.prepareStatement("SELECT id FROM Users WHERE id = ?");
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                // Если такого пользователя нет, добавляем его в базу данных
                pstmt = conn.prepareStatement("INSERT INTO Users (id) VALUES (?)");
                pstmt.setString(1, id);
                pstmt.executeUpdate();
                System.out.println("Пользователь с ID " + id + " добавлен в базу данных");
            } else {
                System.out.println("Пользователь с ID " + id + " уже существует в базе данных");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void addUserEvent(int id, String typeEvent, String nameEvent, int priceEvent, int countTickets, String dataEvent) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // создаем PreparedStatement для выполнения запроса на добавление данных
            conn = DatabaseHandler.getConnection();
            PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO UserEvents (id, event_type, name_, price, number_of_tickets, dataEvent) " +
                            "VALUES (?, ?, ?, ?, ?, ?)");

            // устанавливаем значения параметров запроса
            statement.setInt(1, id);
            statement.setString(2, typeEvent);
            statement.setString(3, nameEvent);
            statement.setInt(4, priceEvent);
            statement.setInt(5, countTickets);
            statement.setString(6, dataEvent);

            // выполняем запрос на добавление данных
            statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Ошибка при добавлении данных в таблицу UserEvents: " + e.getMessage());
        }
    }
}

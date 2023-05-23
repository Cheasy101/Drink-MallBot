package org.example;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.sql.*;

public class Bot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "KazanMallEvent_bot";
    }

    @Override
    public String getBotToken() {
        return "6281846266:AAH90mCXD5EeaJP3l8XBt2z332M9lrZfZzU";
    }

    private enum options {
        BuyingStatus, WORK, COUNTING_TICKETS, ADMINOPTIONS, UPDADESTATUS, DELETESTATUS, ADDSTATUS, FIND, Next, ADMINROOT
    }

    private final List<Long> adminIds = List.of(941087528L);
    private options option = options.WORK;
    int variable;
    Events currentEvent;
    //    long chatId;
    private boolean isAdminMode = false;

    public void onUpdateReceived(Update update) {
        var msg = update.getMessage();
        var user = msg.getFrom();
        var id = user.getId();
        var txt = msg.getText();
//        replyKeyboardMarkup.setOneTimeKeyboard(false);
        System.out.println(update);
        if (update.hasMessage()) {
            switch (txt) {
                case ("/admin") -> {
                    // проверяем, является ли отправитель команды администратором
                    if (adminIds.contains(id)) {
                        isAdminMode = !isAdminMode; // переключаем режим админа
                        String replyText = isAdminMode ? "Режим админа включен" : "Режим админа выключен";
                        sendText(id, replyText);
                        if (isAdminMode) {
                            option = options.ADMINROOT;
                        } else option = options.WORK;

                    } else {
                        sendText(id, "У вас нет прав на выполнение этой команды");
                    }
                }
                case "/start" -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendPhoto(id, "D:\\картинки\\drinkmall.png");
                    String str = "Добро пожаловать в бота KAZAN DRINK-MALL " + EmojiParser.parseToUnicode("‼️") +
                            "\nвы тут впервые " + EmojiParser.parseToUnicode("❓❓") + "\nиспользуйте команду /instruction";
                    sendText(id, str);
                    if (isAdminMode) {
                        option = options.ADMINROOT;
                    } else option = options.WORK;
                }
                case "/instruction" -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendText(id, returnInstruction());
                    if (isAdminMode) {
                        option = options.ADMINROOT;
                    } else option = options.WORK;
                }
                case "/events" -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendText(id, showEvents(id, "Events"));
                    if (isAdminMode) {
                        option = options.ADMINROOT;
                    } else {
                        System.out.println("как бы мы уже не админ");
                        option = options.WORK;
                    }
                }
                case "/buy" -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendText(id, "Выберите мероприятие, на которое хотите пойти ! \n" +
                            "и самое главное: воспользуйтесь либо кнопками сообщения, либо введите айди мероприятия на которое хотите пойти");
                    sendText(id, showEvents(id, "Events"));
                    if (isAdminMode) {
                        option = options.ADMINROOT;
                    } else option = options.BuyingStatus;
                }
                case "/myevents" -> {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    sendText(id, getUserEvents(Math.toIntExact(id)));
                    sendText(id, "Вашему вниманию представлены все мероприятия, на которые вы купили биллеты");
                    if (isAdminMode) {
                        option = options.ADMINROOT;
                    } else {
                        System.out.println("как бы мы уже не админ");
                        option = options.WORK;
                    }
                }
                case "/findbyname" -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    sendText(id, "Данная функция осуществляет поиск мероприятия по названию\n введите название мероприятия");
                    option = options.FIND;
                    System.out.println("Переходим в поиск");
                }
            }
            switch (option) {
                case ADMINROOT -> {
                    System.out.println(" ---- МЫ В ПРАВАХ АДМИНА ----");
                    sendReplyKeyboardMessage(id, "Клава кока", Arrays.asList("Удалить Мероприятие", "Изменить мероприятие", "Добавить Новое мероприятие"));
                    if (isAdminMode) {
                        option = options.ADMINOPTIONS;
                    } else option = options.WORK;
                }
                /**
                 * Блок работает только тогда, когда мы в админе понятное дело
                 */
                case ADMINOPTIONS -> {
                    System.out.println("Я В ОПЦИЯХ АДМИНА");
                    switch (txt) {
                        case "Изменить мероприятие" -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            sendText(id, "Выберите мероприятие (по айди), которое хотите изменить");
                            sendText(id, showEvents(id, "Events"));
                            System.out.println("Должны перейти в состояние изменения мероприятия");
                            option = options.BuyingStatus;
                        }
                        case "Удалить Мероприятие" -> {
                            sendText(id, "Выберите мероприятие (по айди), которое хотите удалить");
                            sendText(id, showEvents(id, "Events"));
                            System.out.println("Должны перейти в состояние удаления мероприятия");
                            option = options.DELETESTATUS;
                        }
                        case "Добавить Новое мероприятие" -> {
                            sendText(id, "Выберите мероприятие (по айди), которое хотите добавить");
                            sendText(id, showEvents(id, "Events"));
                            System.out.println("Должны перейти в состояние добавления мероприятия");
                            option = options.ADDSTATUS;
                        }
                    }
                }
                case BuyingStatus -> {
                    /* я думал по местить в отдельный метод, но решил не засорять методами код. пускай так будет*/
                    variable = Integer.parseInt(txt); // тут выбранный варик
                    currentEvent = getObjectByElement(txt, "id"); // тут получаем все данные о ивенте
                    chosenEvent(id);

                    if (isAdminMode) {
                        sendText(id, "Напишите Полностью все данные, которые собираетесь изменить (и сначала айди в качестве указателя на это мероприятие)");
                        System.out.println("иду в апдейт статусе");
                        option = options.UPDADESTATUS;
                    } else {
                        sendText(id, "Укажите количество билетов, которые вы хотите купить");
                        System.out.println("я в подсчете биллетов");
                        option = options.COUNTING_TICKETS;

                    }
                }
                case COUNTING_TICKETS -> {
                    try {
                        buyTickets(id, txt);
                        System.out.println("я в работе ");
                        option = options.WORK;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                case UPDADESTATUS -> {
                    System.out.println("Пытаемся апдейтить");
                    System.out.printf((txt) + "%n");

                    if (isValidEventData(txt)) {
                        String[] str = txt.split(" ");
                        currentEvent = new Events(Integer.parseInt(str[0]), str[1], str[2],
                                Integer.parseInt(str[3]), Integer.parseInt(str[4]), str[5]);
                        updateDataById(Integer.parseInt(str[0]), currentEvent, "UPDATE Events SET id = ?, event_type = ?, name_ = ?, price = ?, number_of_tickets = ?, dataEvent = ? WHERE id = ?");
                        sendText(id, "Все прошло успешно");
                        System.out.println("я в админе");
                        option = options.ADMINOPTIONS;
                    } else {
                        sendText(id, "Неверный формат данных. Пожалуйста, введите данные в следующем формате: \n'id мероприятия '_' тип мероприятия '_' " +
                                "название мероприятия '_' стоимость биллетов '_' количество билетов '");
                        option = options.UPDADESTATUS;
                    }
                }
                case DELETESTATUS -> {

                    variable = Integer.parseInt(txt); // тут выбранный варик
                    currentEvent = getObjectByElement(txt, "id"); // тут получаем все данные о ивенте
                    chosenEvent(id);
                    // TODO: 21.05.2023  если чето не сработает, то проверку на админа вставить
                    deleteEventById(variable);
                    sendText(id, "Вот список текущих мероприятий");
                    sendText(id, showEvents(id, "Events"));
//                    option = options.WORK;
                    System.out.println("я в админе");

                    option = options.ADMINOPTIONS;
                }
                case ADDSTATUS -> {
                    sendText(id, "Ща обновимся");
                    String[] str = txt.split(" ");

                    try {
                        if (str.length < 5) {
                            throw new Exception(" недостаточно аргументов\n");
                        }

                        Events currentEvent = new Events(str[0], str[1], Integer.parseInt(str[2]),
                                Integer.parseInt(str[3]), str[4]);

                        System.out.println(currentEvent);
                        addEvent(currentEvent);
                        sendText(id, "Мероприятие добавлено успешно");
                        System.out.println("Идем в режим админа");
                        option = options.ADMINOPTIONS;

                    } catch (NumberFormatException e) {
                        System.out.println(e.getMessage());
//                        sendText(id,"Неправильный формат данных. попробуйте еще раз \n");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        sendText(id, "Неправильный формат данных:\n " + e.getMessage());
                    }
                }
                case Next -> {
                    try {
                        if (getObjectByElement(txt, "name_").toString() == null) {
                            throw new Exception("Такого мероприятия у нас нет");
                        }
                        sendText(id, getObjectByElement(txt, "name_").toString());

                    } catch (Exception e) {
                        sendText(id, e.getMessage());
                        throw new RuntimeException(e);
                    }
                    if (isAdminMode) {
                        option = options.ADMINROOT;
                    } else option = options.WORK;
                }
                case FIND -> {

                    if (isAdminMode) {
                        option = options.ADMINROOT;
                    } else option = options.Next;
                }
            }
        }
    } // тут тхт выступает как кол-во билетов

    public String getUserEvents(int userId) {
        String str;
        str = "";
        try {
            // Открываем соединение с базой данных
            Connection conn = DatabaseHandler.getConnection();

            // Создаем SQL-запрос для получения всех записей с заданным id пользователя
            String sql = "SELECT * FROM UserEvents WHERE id = ?";

            // Создаем PreparedStatement для выполнения запроса и передаем параметры
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);

            // Выполняем запрос и получаем результат
            ResultSet resultSet = pstmt.executeQuery();

            // Обрабатываем результат и выводим данные
            while (resultSet.next()) {
                int eventId = resultSet.getInt("id");

                String eventType = resultSet.getString("event_type");

                String eventName = resultSet.getString("name_");
                int eventPrice = resultSet.getInt("price");
                int eventNumberOfTickets = resultSet.getInt("number_of_tickets");
                String eventData = resultSet.getString("dataEvent");

                str += "\uD83C\uDD94 -> " + eventId + " Вид Мероприятия\uD83D\uDC49  " + eventType
                        + "\nНазвание меро\uD83D\uDC49 " + eventName + " \uD83D\uDCB2 "
                        + eventPrice + "₽ \nКол-во Биллетов " + eventNumberOfTickets + "шт. \uD83D\uDDD3️ дата меро " + eventData + "\n";
            }

            // Закрываем все ресурсы
            resultSet.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving user events: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return str;
    }

    private void addEvent(Events event) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseHandler.getConnection();
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement("INSERT INTO Events (event_type ,name_,price,number_of_tickets, dataEvent) VALUES (?,?,?,?,?);");

//            INSERT INTO Events (event_type ,name_,price,number_of_tickets, dataEvent) VALUES ('dad','mom',2,3,'2123')
            System.out.println(event.toString());
            pstmt.setString(1, event.getEventType());
            pstmt.setString(2, event.getEventName());
            pstmt.setInt(3, event.getEventPrice());
            pstmt.setInt(4, event.getEventNumberOfTickets());
            pstmt.setString(5, event.getEventData());
            System.out.println(pstmt);
            pstmt.executeUpdate();
            // подтверждаем транзакцию
            conn.commit();
            System.out.println("Должен был добавить");
        } catch (SQLException e) {
            assert conn != null;
            conn.rollback(); // откатываем транзакцию, если произошла ошибка
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            assert conn != null;
            conn.setAutoCommit(true); // возвращаем в автоматический режим фиксации
            conn.close(); // закрываем подключение
        }
    }

    public void deleteEventById(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // создаем подключение к базе данных
            conn = DatabaseHandler.getConnection();
            // создаем запрос на удаление данных с указанным id
            pstmt = conn.prepareStatement("DELETE FROM Events WHERE id = ?");
            pstmt.setInt(1, id);
            // выполняем запрос на удаление
            pstmt.executeUpdate();
            // обновляем id в оставшихся записях
            pstmt = conn.prepareStatement("UPDATE Events SET id = id - 1 WHERE id > ?");
            pstmt.setInt(1, id);
            // выполняем запрос на обновление
            pstmt.executeUpdate();
            // подтверждаем транзакцию
            conn.commit();
            System.out.println("Должен был удалить");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                // закрываем все ресурсы
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void chosenEvent(Long id) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendText(id, "вы выбрали мероприятие --> " + this.currentEvent);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isValidEventData(String messageText) {
        String[] str = messageText.split(" ");
        if (str.length != 6) {
            return false;
        }
        try {
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void buyTickets(Long id, String numberOfTickets) throws InterruptedException {
        Thread.sleep(1000); // пауза на 5 секунд
        sendText(id, "Вы выбрали " + numberOfTickets + " биллетов. \n осуществляем транзакцию....");
        User user = new User();
        user.addUserInUsersDatabase(String.valueOf(id)); // проверяем и если что закидываем в бд
        int numTickets = Integer.parseInt(numberOfTickets);

        currentEvent = new Events(variable, currentEvent.getEventType(), currentEvent.getEventName(),
                currentEvent.getEventPrice(), currentEvent.getEventNumberOfTickets() - numTickets,
                currentEvent.getEventData());
//        updateDataById(variable, currentEvent, "UPDATE Events SET id = ?, event_type = ?, name_ = ?, price = ?, number_of_tickets = ?, dataEvent = ? WHERE id = ?");

        updateDataById(variable, currentEvent, "UPDATE Events SET id = ?, event_type = ?, name_ = ?, price = ?, number_of_tickets = ?, dataEvent = ? WHERE id = ?");

        user.addUserEvent(Integer.parseInt(String.valueOf(id)),
                currentEvent.getEventType(),
                currentEvent.getEventName(),
                currentEvent.getEventPrice(),
                numTickets,
                currentEvent.getEventData());
        Thread.sleep(2000);
        sendText(id, "Оплата совершена успешно‼️‼️ ✅✅✅");
        sendPhoto(id, "D:\\картинки\\succes.jpg");
    }

    public void updateDataById(int id, Events newData, String sqlRequest) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {

            conn = DatabaseHandler.getConnection();
//            String sql = "UPDATE Events SET id = ?, event_type = ?, name_ = ?, price = ?, number_of_tickets = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sqlRequest);

            pstmt.setInt(1, id);
            pstmt.setString(2, newData.getEventType());
            pstmt.setString(3, newData.getEventName());
            pstmt.setInt(4, newData.getEventPrice());
            pstmt.setInt(5, newData.getEventNumberOfTickets());
            pstmt.setString(6, newData.getEventData());
            pstmt.setInt(7, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Events getObjectByElement(String id, String element) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Events chosenEvent = null;

        System.out.println("ID -> " + id + "\n element -> " + element);

        try {
            connection = DatabaseHandler.getConnection();
            statement = connection.prepareStatement("SELECT * FROM Events WHERE " + element + " = ?");

            System.out.println("я после запроса");
            statement.setString(1, id);
            resultSet = statement.executeQuery();
            System.out.println(statement);
            System.out.println(resultSet.toString());
            if (resultSet.next()) {
                // create object with retrieved data
                chosenEvent = new Events(resultSet.getInt("id"),
                        resultSet.getString("event_type"), resultSet.getString("name_"),
                        resultSet.getInt("price"), resultSet.getInt("number_of_tickets"), resultSet.getString("dataEvent"));
            }
            System.out.println(chosenEvent.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return chosenEvent;
    }

    public String showEvents(Long id, String Database) {
        Connection connection = null;
        String str;
        try {
            connection = DatabaseHandler.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + Database);

            sendText(id, "Вот список всех доступных мероприятий !");
            str = "";

            while (resultSet.next()) {
                int eventId = resultSet.getInt("id");

                String eventType = resultSet.getString("event_type");

                String eventName = resultSet.getString("name_");
                int eventPrice = resultSet.getInt("price");
                int eventNumberOfTickets = resultSet.getInt("number_of_tickets");
                String eventData = resultSet.getString("dataEvent");
                str += "\uD83C\uDD94 -> " + eventId + " Вид Мероприятия\uD83D\uDC49  " + eventType
                        + "\nНазвание меро\uD83D\uDC49 " + eventName + " \uD83D\uDCB2 "
                        + eventPrice + "₽ \nКол-во Биллетов " + eventNumberOfTickets + "шт. \uD83D\uDDD3️ дата меро " + eventData + "\n";
            }
            connection.close();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return str;
    }

    static String returnInstruction() {
        String filePath = "C:\\Users\\Bulat\\IdeaProjects\\KazanDrinkMallBot\\src\\main\\java\\org\\example\\instruction";
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPhoto(long id, String pathToPhoto) {
        // создаем объект InputFile из файла
        File photo = new File(pathToPhoto);
        InputFile inputFile = new InputFile(photo);
        // создаем объект SendPhoto и добавляем в него InputFile
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(id));
        sendPhoto.setPhoto(inputFile);
        // отправляем фото
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendText(Long who, String what) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(who.toString()) // Who are we sending a message to
                .text(what).build();   // Message content
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendReplyKeyboardMessage(Long chatId, String text, List<String> options) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString()).
                text(text)
                .replyMarkup(getReplyKeyboardMarkup(options)).build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup getReplyKeyboardMarkup(List<String> options) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        for (String option : options) {
            KeyboardRow row = new KeyboardRow();
            row.add(option);
            keyboard.add(row);
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }
}
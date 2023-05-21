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
        BuyingStatus, WORK, COUNTING_TICKETS, ADMINROOT
    }

    private List<Long> adminIds = List.of(941087528L);
    private options option = options.WORK;
    int variable;
    Events currentEvent;
    //    long chatId;
    private boolean isAdminMode = false;
    static ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

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
                            sendText(id, "ща клава вылезет");
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
                    option = options.WORK;
                }
                case "/instruction" -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendText(id, returnInstruction());
                    option = options.WORK;
                }
                case "/events" -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("должно 1");
                    sendText(id, showEvents(id));
                    System.out.println("должно 2");
                    option = options.WORK;
                }
                case "/buy" -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendText(id, "Выберите мероприятие, на которое хотите пойти ! \n" +
                            "и самое главное: воспользуйтесь либо кнопками сообщения, либо введите айди мероприятия на которое хотите пойти");
//                    operateFunction(id, showEvents(id));
                    sendText(id, showEvents(id));
                    option = options.BuyingStatus; // TODO: 19.05.2023 в идее добавить сюда клаву к сообщению
                }
            }
            switch (option) {
                case ADMINROOT -> {
                    sendReplyKeyboardMessage(id, "Клава кока", Arrays.asList("Удалить Мероприятие", "Изменить мероприятие", "Добавить Новое мероприятие"));
                }

                case BuyingStatus -> {
                    /* я думал по местить в отдельный метод, но решил не засорять методами код. пускай так будет*/
                    variable = Integer.parseInt(txt); // тут выбранный варик
                    currentEvent = getObjectById(txt); // тут получаем все данные о ивенте
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendText(id, "вы выбрали мероприятие --> " + currentEvent);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendText(id, "Укажите количество билетов, которые вы хотите купить");
                    option = options.COUNTING_TICKETS;
                }
                case COUNTING_TICKETS -> {
                    try {
                        buyTickets(id, txt);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } // тут тхт выступает как кол-во билетов
        }
    }

    private void buyTickets(Long id, String numberOfTickets) throws InterruptedException {
        Thread.sleep(1000); // пауза на 5 секунд
        sendText(id, "Вы выбрали " + numberOfTickets + " биллетов. \n осуществляем транзакцию....");
        int numTickets = Integer.parseInt(numberOfTickets);

        currentEvent = new Events(variable, currentEvent.getEventType(), currentEvent.getEventName(),
                currentEvent.getEventPrice(), currentEvent.getEventNumberOfTickets() - numTickets);
        updateDataById(variable, currentEvent, "UPDATE Events SET id = ?, event_type = ?, name_ = ?, price = ?, number_of_tickets = ? WHERE id = ?");
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
            pstmt.setInt(6, id);
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

    public Events getObjectById(String id) {
        System.out.println("я не работаю");
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Events chosenEvent = null;

        try {
            connection = DatabaseHandler.getConnection();
            statement = connection.prepareStatement("SELECT * FROM Events WHERE id = ?");
            statement.setInt(1, Integer.parseInt(id));
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                // create object with retrieved data
                chosenEvent = new Events(resultSet.getInt("id"), resultSet.getString("event_type"), resultSet.getString("name_"), resultSet.getInt("price"), resultSet.getInt("number_of_tickets"));
//                chosenEvent = new Object(resultSet("id"), resultSet.getString("name"), resultSet.getInt("age"));
            }
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

    public String showEvents(Long id) {
        System.out.println(" я работаю");
        Connection connection = null;
        String str;
        try {
            System.out.println("вот тут все норм");
            connection = DatabaseHandler.getConnection();

            Statement statement = connection.createStatement();
            System.out.println("вот тут все норм");
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Events");
            System.out.println("даже запрос отправляю");

            sendText(id, "Вот список всех доступных мероприятий !");
            str = "";

            while (resultSet.next()) {
                int eventId = resultSet.getInt("id");

                String eventType = resultSet.getString("event_type");

                String eventName = resultSet.getString("name_");
                int eventPrice = resultSet.getInt("price");
                int eventNumberOfTickets = resultSet.getInt("number_of_tickets");

                str += eventId + " " + eventType + " " + eventName + " " + eventPrice + " " + eventNumberOfTickets + "\n";
            }
//            sendText(id, str);
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
//        SendMessage message = SendMessage.builder()
//                .chatId(chatId.toString()) // Who are we sending a message to
//                .text(text).build().setReplyMarkup(getReplyKeyboardMarkup(options));
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
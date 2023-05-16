package com.example.SpringBot.service;

import com.example.SpringBot.config.BotConfig;
import com.example.SpringBot.model.*;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {


    @Autowired
    private final SalaryServices salaryServices;
    @Autowired
    private final JokeServices jokeServices;
    final BotConfig config;
    final String YES = "YES_BUTTON";
    final String NO = "NO_BUTTON";
    static final String HELP_TEXT = "Бот умеет считать ЗП при вводе выручки\nНапример: salary 150300\nНа это пока всё :)";

    @Autowired
    public TelegramBot(SalaryServices salaryServices, JokeServices jokeServices, BotConfig config) {
        this.salaryServices = salaryServices;
        this.jokeServices = jokeServices;
        this.config = config;
//        List<BotCommand> listOfCommand = new ArrayList<>();
//        listOfCommand.add(new BotCommand("/start", "Начало работы"));
//        listOfCommand.add(new BotCommand("/help", "Информация о боте"));
//        listOfCommand.add(new BotCommand("/toDo", "Тут ещё будут команды"));
//        try {
//            this.execute(new SetMyCommands(listOfCommand, new BotCommandScopeDefault(), null));
//        } catch (TelegramApiException e) {
//            log.error("Error setting bot's command list: " + e.getMessage());
//        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String msgText = update.getMessage().getText();
            String[] msgParse = msgText.split(" ");
            String message = msgParse[0];
            String data = "";
            String comment = "";
            switch (msgParse.length) {
                case 2:
                    data = msgParse[1];
                    break;
                case 3:
                    data = msgParse[1];
                    comment = msgParse[2];
                    break;
            }
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getUserName();

            switch (message.toLowerCase()) {
                case "/start":
                    startCommand(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "salary":
                    salarySave(chatId, data, update.getMessage());
                    break;
                case "+зп":
                    salaryServices.addMoney(chatId, data, comment, update.getMessage());
                    sendMessage(chatId, "Всё ОК)");
                    break;
                case "вчера":
                    salaryYesterday(chatId, userName);
                    break;
                case "хочешь_шутку?":
                    get(chatId);
                    break;
                case "моя_зарплата":
                    getByAllSalary(chatId, userName);
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "Я не знаю такой команды");
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String text = "";

            if (callbackData.equals(YES)) {
                text = jokeServices.getJoke();
            } else {
                text = "Ну как хочешь O_o";
            }
            EditMessageText msg = new EditMessageText();
            msg.setChatId(chatId);
            msg.setText(text);
            msg.setMessageId(messageId);

            try {
                execute(msg);
            } catch (TelegramApiException e) {
                log.error("Error setting bot's command list: " + e.getMessage());
            }
        }


    }



    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Хочешь_шутку?");

//        row.add("weather");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("моя_зарплата");
//        row.add("check data");
//        row.add("delite data");
        keyboardRows.add(row);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);


        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    private void startCommand(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Привет, " + name + " :blush:");
        log.info("Replace to user" + name);
        sendMessage(chatId, answer);
    }

    private void salarySave(long chatId, String revenue, Message msg){
        int salary = salaryServices.calculateSalary(chatId, revenue, msg);
        String textSend = Integer.toString(salary);
        sendMessage(chatId, textSend);
    }


    private void get(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Точно хочешь шутку, тебя предупреждали?");

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData(YES);

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData(NO);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        inlineKeyboard.setKeyboard(rowsInLine);
        message.setReplyMarkup(inlineKeyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }

    }

    public void getByAllSalary(long chatId, String name){
        String textSend = salaryServices.getAllSalary(name);
//        String textSend = salaryServices.getAllSalary("Ray");
        sendMessage(chatId, textSend);
    }

    private void salaryYesterday(long chatId, String name) {
        String textSend = salaryServices.getSalaryYesterday(name);
//        String textSend = salaryServices.getAllSalary("Ray");
        sendMessage(chatId, textSend);
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

}

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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private SalaryInterface salaryInterface;
    final BotConfig config;
    final String YES = "YES_BUTTON";
    final String NO = "NO_BUTTON";
    static final String HELP_TEXT = "Бот умеет считать ЗП при вводе выручки\nНапример: salary 150300\nНа это пока всё :)";


    public TelegramBot(BotConfig config){
        this.config = config;
        List<BotCommand> listofCommand = new ArrayList<>();
        listofCommand.add(new BotCommand("/start", "get a welcome message"));
        listofCommand.add(new BotCommand("/mydata", "get your data stored"));
        listofCommand.add(new BotCommand("/deletedata", "delete my data"));
        listofCommand.add(new BotCommand("/help", "info how bot"));
        listofCommand.add(new BotCommand("/setting", "set your preferences"));
        try {
            this.execute(new SetMyCommands(listofCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e){
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String msgText = update.getMessage().getText();
            String[] msgParse = msgText.split(" ");
            String message = msgParse[0];
            String data = "";
            if(msgParse.length > 1){
                data = msgParse[1];
            }
            long chatId = update.getMessage().getChatId();

            switch (message){
                case"/start":
                    startCommand(chatId, update.getMessage().getChat().getFirstName());
                    registerUser(chatId, update.getMessage());
                    break;
                case "salary":
                    compareSalariesCommand(chatId, data, update.getMessage());
                    break;
                case "get":
                    // TO DO
                    break;
                case "Хочешь_шутку?":
                    get(chatId);
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default: sendMessage(chatId, "Я не знаю такой команды");
            }
        } else if(update.hasCallbackQuery()){
            String callbackData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String text = "";

            if(callbackData.equals(YES)){
//                text = command.getJoke();
                text = "Шутить про рост - низко.\nДавайте будем выше этого.";
            } else{
                text = "Ну как хочешь O_o";
            }
            EditMessageText msg = new EditMessageText();
            msg.setChatId(chatId);
            msg.setText(text);
            msg.setMessageId(messageId);

            try{
                execute(msg);
            } catch (TelegramApiException e){
                log.error("Error setting bot's command list: " + e.getMessage());
            }
        }


    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Хочешь_шутку?");

//        row.add("weather");
//        keyboardRows.add(row);
//        row = new KeyboardRow();
//        row.add("register");
//        row.add("check data");
//        row.add("delite data");
        keyboardRows.add(row);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);


        try{
            execute(message);
        } catch (TelegramApiException e){
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    private void startCommand(long chatId, String name){
        String answer = EmojiParser.parseToUnicode("Привет, " + name + " :blush:");
        log.info("Replace to user" + name);
        sendMessage(chatId, answer);
    }

    private void registerUser(long chatId, Message msg) {
        if(userRepo.findById(chatId).isEmpty()){
            var chat = msg.getChat();
            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userRepo.save(user);
            log.info("User save" + user);

        }
    }

    private void compareSalariesCommand(long chatId, String revenue, Message msg){
        int money = Integer.parseInt(revenue);
//        String name = msg.getChat().getUserName();
        String name = "Ray";
        int salary;
        if(name.equals("l01d3n")){
            salary = (int) (((money * 0.02) - 300)/10);
        } else{
            salary = (int) (((money * 0.02) + 200)/10);
        }
        salary = salary*10;
        String textSend = Integer.toString(salary);
        saveSalary(salary, name);
        sendMessage(chatId, textSend);
    }

    private void saveSalary(int salary, String name) {
        Salary sal = new Salary();
        sal.setMoney(salary);
        sal.setDate(LocalDate.now());
//        sal.setDate(LocalDate.of(2023, 04, 19));
        sal.setUserName(name);
        salaryInterface.save(sal);
    }

    private void get(long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Точно хочешь шутку, тебя предупреждали?");

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData(YES);

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData(NO);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        inlineKeyboard.setKeyboard(rowsInLine);
        message.setReplyMarkup(inlineKeyboard);

        try{
            execute(message);
        } catch (TelegramApiException e){
            log.error("Error setting bot's command list: " + e.getMessage());
        }

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

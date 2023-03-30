package com.example.SpringBot.service;

import com.example.SpringBot.config.BotConfig;
import com.example.SpringBot.model.Salary;
import com.example.SpringBot.model.SalaryInterface;
import com.example.SpringBot.model.User;
import com.example.SpringBot.model.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
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
                    compareSalariesCommand(chatId, data);
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default: sendMessage(chatId, "Я не знаю такой команды");
            }
        }

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

    private void startCommand(long chatId, String name){
        String answer = "Привет, " + name + "  :)";
        log.info("Replace to user" + name);
        sendMessage(chatId, answer);
    }
    private void compareSalariesCommand(long chatId, String revenue){
        int money = Integer.parseInt(revenue);
        int salary = (int) (((money * 0.02) - 300)/10);
        salary = salary*10;
        String textSend = Integer.toString(salary);
        sendMessage(chatId, textSend);
        saveSalary(salary);
    }

    private void saveSalary(int salary) {
        Salary sal = new Salary();
        sal.setMoney(salary);
        sal.setDate(LocalDate.now());
        salaryInterface.save(sal);

    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

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

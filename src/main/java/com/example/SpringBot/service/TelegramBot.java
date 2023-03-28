package com.example.SpringBot.service;

import com.example.SpringBot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;

    public TelegramBot(BotConfig config){
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String msgText = update.getMessage().getText();
            String[] msgParse = msgText.split(" ");
            String command = msgParse[0];
            String data = "";
            if(msgParse.length > 1){
                data = msgParse[1];
            }
            long chatId = update.getMessage().getChatId();

            switch (command){
                case"/start":
                    startCommand(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "salary":
                    compareSalariesComand(chatId, data);
                    break;
                default: sendMessage(chatId, "Я не знаю такой команды");
            }
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

    private void startCommand(long chatId, String name){
        String answer = "Привет, " + name + "  :)";
        sendMessage(chatId, answer);

    }

    private void compareSalariesComand(long chatId, String revenue){
        int money = Integer.parseInt(revenue);
        int salary = (int) (((money * 0.02) - 300)/10);
        salary = salary*10;
        String textSend = Integer.toString(salary);
        sendMessage(chatId, textSend);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try{
            execute(message);
        } catch (TelegramApiException e){
        }
    }
}

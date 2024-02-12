package com.example.SpringBot.service;

import com.example.SpringBot.model.Salary;
import com.example.SpringBot.repository.SalaryRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SalaryServices {

    @Autowired
    public final SalaryRepository salaryRepository;

    public SalaryServices(SalaryRepository salaryRepository) {
        this.salaryRepository = salaryRepository;
    }

    @Transactional
    public int calculateSalary(String revenue, String name) {
        int money = Integer.parseInt(revenue);

        int salary = (int) (money * 0.02);
        if (salary < 1300) {
            salary = 1300;
        }
        if (name.equals("l01d3n")) {
            salary = (salary - 300) / 10;
        } else if (name.equals("Raisa76")) {
            salary = (salary + 200) / 10;
        } else {
            salary = (salary + 100) / 10;

        }
        salary = salary * 10;
        save(salary, name, "зп");
        return salary;

    }

    @Transactional
    public void save(int money, String name, String comment) {
        LocalDate date = LocalDate.now();
        Salary salary;
        salary = findByDateAndUserNameAndComment(date, name, comment);
        if (salary == null) {
            salary = new Salary(date, money, name, comment);
        } else {
            salary.setMoney(money);
        }
        salaryRepository.save(salary);

    }

    @Transactional
    public void addMoney(String data, String comment, Message msg) {
        int money = Integer.parseInt(data);
        String name = msg.getChat().getUserName();
        save(money, name, comment);
    }

    private List<Salary> findByUserName(String name) {
        List<Salary> userSalary = salaryRepository.findByUserName(name);
        return userSalary;
    }

    public String getAllSalary(String name) {
        List<Salary> salary = findByUserName(name).stream().sorted(Comparator.comparingInt(Salary::getId)).collect(Collectors.toList());
        return salaryToString(salary);
    }

    public String getLastSalary(String name) {
        Salary salary = salaryRepository.findFirstByUserNameOrderByIdDesc(name);
        return String.valueOf(salary);
    }

    private String salaryToString(List<Salary> salary) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Salary s : salary) {
            sb.append(++i + ".    " + s.toString() + "\n");
        }
        String stringSalary = sb.toString();
        return stringSalary;
    }

    public String getSalaryInAMonth(String userName, LocalDate startDate, LocalDate endDate) {
        List<Salary> salaryInAMonth = salaryRepository.findByUserNameAndDateBetween(userName, startDate, endDate);
        return salaryToString(salaryInAMonth);
    }

    public int getSumSalaryInAMonth(String userName, LocalDate startDate, LocalDate endDate) {
        int salaryInAMonth = salaryRepository.findSumInAMonth(userName, startDate, endDate);
        return salaryInAMonth;

    }

    private Salary findByDateAndUserNameAndComment(LocalDate date, String name, String comment) {
        Salary salary = salaryRepository.findByDateAndUserNameAndComment(date, name, comment);
        return salary;
    }

    public void exportToExcel() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Salary");

        List<Salary> listSalary = salaryRepository.findAll();
//        List<Salary> listSalary = salaryRepository.findByUserNameAndDateBetween("l01d3n",
//                LocalDate.of(2024, 01, 01),
//                LocalDate.of(2024, 01, 10));
        writeHeaderLine(sheet, workbook);
        writeDataLines(sheet, workbook, listSalary);

        try {
            String path = "src\\main\\resources\\doc\\salary_" + LocalDate.now() + ".xlsx";
            FileOutputStream out = new FileOutputStream(
                    new File(path));
            workbook.write(out);
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void writeHeaderLine(XSSFSheet sheet, XSSFWorkbook workbook) {
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        createCell(row, 0, "ID", style, sheet);
        createCell(row, 1, "Data", style, sheet);
        createCell(row, 2, "Name", style, sheet);
        createCell(row, 3, "Salary", style, sheet);
        createCell(row, 4, "Comment", style, sheet);
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style, XSSFSheet sheet) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else {
            String date = String.valueOf(value);
            cell.setCellValue(date);
        }
        cell.setCellStyle(style);
    }

    private void writeDataLines(XSSFSheet sheet, XSSFWorkbook workbook, List<Salary> listSalary) {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (Salary salary : listSalary) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, salary.getId(), style, sheet);
            createCell(row, columnCount++, salary.getDate(), style, sheet);
            createCell(row, columnCount++, salary.getUserName(), style, sheet);
            createCell(row, columnCount++, salary.getMoney(), style, sheet);
            createCell(row, columnCount++, salary.getComment(), style, sheet);
        }
    }

}
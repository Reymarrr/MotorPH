import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class MotorPHv2

 {
    static final String EMPLOYEE_FILE = "Employee Details.csv";
    static final String ATTENDANCE_FILE = "Attendance Record.csv";
    static final String PASSWORD = "12345";

    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    static final LocalTime WORK_START = LocalTime.of(8, 0);
    static final LocalTime GRACE_LIMIT = LocalTime.of(8, 5);
    static final LocalTime WORK_END = LocalTime.of(17, 0);

    static final BigDecimal SIXTY = new BigDecimal("60");
    static final BigDecimal SSS_MIN_SALARY = new BigDecimal("3250");
    static final BigDecimal SSS_MAX_SALARY = new BigDecimal("24750");
    static final BigDecimal SSS_STEP = new BigDecimal("500");
    static final BigDecimal SSS_BASE = new BigDecimal("135");
    static final BigDecimal SSS_INCREMENT = new BigDecimal("22.5");
    static final BigDecimal ZERO = BigDecimal.ZERO;

    static final int HOURS_FIRST = 0;
    static final int HOURS_SECOND = 1;
    static final int GROSS_FIRST = 2;
    static final int GROSS_SECOND = 3;
    static final int SSS = 4;
    static final int PHILHEALTH = 5;
    static final int PAGIBIG = 6;
    static final int TAX = 7;
    static final int TOTAL_DEDUCTIONS = 8;
    static final int NET_FIRST = 9;
    static final int NET_SECOND = 10;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        printSeparator("=");
        System.out.println("        MOTORPH PAYROLL");
        printSeparator("=");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (!isValidCredentials(username, password)) {
            System.out.println("Incorrect username and/or password.");
            scanner.close();
            return;
        }

        if ("employee".equals(username)) {
            runEmployeeMenu(scanner);
        } else {
            runPayrollStaffMenu(scanner);
        }

        scanner.close();
    }

    static boolean isValidCredentials(String username, String password) {
        return ("employee".equals(username) || "payroll_staff".equals(username))
            && PASSWORD.equals(password);
    }

    static void runEmployeeMenu(Scanner scanner) {
        while (true) {
            System.out.println();
            printSeparator("-");
            System.out.println("EMPLOYEE MENU");
            printSeparator("-");
            System.out.println("[1] Enter your employee number");
            System.out.println("[2] Exit the program");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if ("1".equals(choice) || "enter your employee number".equals(choice)) {
                System.out.print("Enter your employee number: ");
                String employeeNumber = scanner.nextLine().trim();
                String[] employee = findEmployee(employeeNumber);

                if (employee == null) {
                    System.out.println("Employee number does not exist.");
                } else {
                    printEmployeeDetails(employee);
                }
            } else if ("2".equals(choice) || "exit the program".equals(choice)) {
                System.out.println("Program terminated.");
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    static void runPayrollStaffMenu(Scanner scanner) {
        while (true) {
            System.out.println();
            printSeparator("-");
            System.out.println("PAYROLL STAFF MENU");
            printSeparator("-");
            System.out.println("[1] Process Payroll");
            System.out.println("[2] Exit the program");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if ("1".equals(choice) || "process payroll".equals(choice)) {
                runProcessPayrollMenu(scanner);
            } else if ("2".equals(choice) || "exit the program".equals(choice)) {
                System.out.println("Program terminated.");
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    static void runProcessPayrollMenu(Scanner scanner) {
        while (true) {
            System.out.println();
            printSeparator("-");
            System.out.println("PROCESS PAYROLL");
            printSeparator("-");
            System.out.println("[1] One employee");
            System.out.println("[2] All employees");
            System.out.println("[3] Exit the program");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if ("1".equals(choice) || "one employee".equals(choice)) {
                System.out.print("Enter the employee number: ");
                String employeeNumber = scanner.nextLine().trim();
                String[] employee = findEmployee(employeeNumber);

                if (employee == null) {
                    System.out.println("Employee number does not exist.");
                } else {
                    printPayrollReport(employee);
                }
            } else if ("2".equals(choice) || "all employees".equals(choice)) {
                printPayrollForAllEmployees();
            } else if ("3".equals(choice) || "exit the program".equals(choice)) {
                return;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    static String[] findEmployee(String employeeNumber) {
        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] row = splitCsv(line);
                if (row.length > 18 && row[0].equals(employeeNumber)) {
                    return row;
                }
            }
        } catch (IOException exception) {
            System.out.println("Unable to read employee file.");
        }

        return null;
    }

    static void printPayrollForAllEmployees() {
        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] employee = splitCsv(line);
                if (employee.length > 18) {
                    printPayrollReport(employee);
                }
            }
        } catch (IOException exception) {
            System.out.println("Unable to read employee file.");
        }
    }

    static void printEmployeeDetails(String[] employee) {
        System.out.println();
        printSeparator("=");
        System.out.println("EMPLOYEE DETAILS");
        printSeparator("=");
        System.out.println("Employee Number : " + employee[0]);
        System.out.println("Employee Name   : " + buildEmployeeName(employee));
        System.out.println("Birthday        : " + employee[3]);
        printSeparator("=");
    }

    static void printPayrollReport(String[] employee) {
        String employeeNumber = employee[0];
        String employeeName = buildEmployeeName(employee);
        String birthday = employee[3];
        BigDecimal hourlyRate = parseAmount(employee[18]);

        System.out.println();
        printSeparator("=");
        System.out.println("PAYROLL REPORT");
        printSeparator("=");
        System.out.println("Employee #     : " + employeeNumber);
        System.out.println("Employee Name  : " + employeeName);
        System.out.println("Birthday       : " + birthday);
        System.out.println("Hourly Rate    : PHP " + formatAmount(hourlyRate));
        printSeparator("=");

        for (int month = 6; month <= 12; month++) {
            BigDecimal[] payroll = computeMonthlyPayroll(employeeNumber, hourlyRate, month);
            int lastDay = YearMonth.of(2024, month).lengthOfMonth();

            System.out.println();
            printSeparator("-");
            System.out.println("Cutoff Date       : " + monthName(month) + " 1 to " + monthName(month) + " 15");
            System.out.println("Total Hours Worked: " + formatAmount(payroll[HOURS_FIRST]));
            System.out.println("Gross Salary      : PHP " + formatAmount(payroll[GROSS_FIRST]));
            System.out.println("Net Salary        : PHP " + formatAmount(payroll[NET_FIRST]));

            System.out.println();
            printSeparator("-");
            System.out.println("Cutoff Date       : " + monthName(month) + " 16 to " + monthName(month) + " " + lastDay);
            System.out.println("Second payout includes all deductions.");
            System.out.println("Total Hours Worked: " + formatAmount(payroll[HOURS_SECOND]));
            System.out.println("Gross Salary      : PHP " + formatAmount(payroll[GROSS_SECOND]));
            printSeparator(".");
            System.out.println("Each Deduction:");
            System.out.println("SSS             : PHP " + formatAmount(payroll[SSS]));
            System.out.println("PhilHealth      : PHP " + formatAmount(payroll[PHILHEALTH]));
            System.out.println("Pag-IBIG        : PHP " + formatAmount(payroll[PAGIBIG]));
            System.out.println("Tax             : PHP " + formatAmount(payroll[TAX]));
            printSeparator(".");
            System.out.println("Total Deductions: PHP " + formatAmount(payroll[TOTAL_DEDUCTIONS]));
            System.out.println("Net Salary      : PHP " + formatAmount(payroll[NET_SECOND]));
        }
        printSeparator("=");
    }

    static BigDecimal[] computeMonthlyPayroll(String employeeNumber, BigDecimal hourlyRate, int month) {
        int firstCutoffMinutes = 0;
        int secondCutoffMinutes = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(ATTENDANCE_FILE))) {
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] row = splitCsv(line);
                if (row.length < 6 || !row[0].equals(employeeNumber)) {
                    continue;
                }

                LocalDate date = parseDate(row[3]);
                if (date == null || date.getYear() != 2024 || date.getMonthValue() != month) {
                    continue;
                }

                int workedMinutes = computeWorkedMinutes(row[4], row[5]);
                if (date.getDayOfMonth() <= 15) {
                    firstCutoffMinutes += workedMinutes;
                } else {
                    secondCutoffMinutes += workedMinutes;
                }
            }
        } catch (IOException exception) {
            System.out.println("Unable to read attendance file.");
        }

        BigDecimal hoursFirst = minutesToHours(firstCutoffMinutes);
        BigDecimal hoursSecond = minutesToHours(secondCutoffMinutes);
        BigDecimal grossFirst = minutesToAmount(firstCutoffMinutes, hourlyRate);
        BigDecimal grossSecond = minutesToAmount(secondCutoffMinutes, hourlyRate);
        BigDecimal combinedGross = grossFirst.add(grossSecond);

        BigDecimal sss = computeSss(combinedGross);
        BigDecimal philHealth = computePhilHealth(combinedGross);
        BigDecimal pagIbig = computePagIbig(combinedGross);
        BigDecimal taxableIncome = combinedGross.subtract(sss).subtract(philHealth).subtract(pagIbig);
        if (taxableIncome.compareTo(ZERO) < 0) {
            taxableIncome = ZERO;
        }

        BigDecimal tax = computeWithholdingTax(taxableIncome);
        BigDecimal totalDeductions = sss.add(philHealth).add(pagIbig).add(tax);
        BigDecimal netFirst = grossFirst;
        BigDecimal netSecond = grossSecond.subtract(totalDeductions);

        return new BigDecimal[] {
            normalize(hoursFirst),
            normalize(hoursSecond),
            normalize(grossFirst),
            normalize(grossSecond),
            normalize(sss),
            normalize(philHealth),
            normalize(pagIbig),
            normalize(tax),
            normalize(totalDeductions),
            normalize(netFirst),
            normalize(netSecond)
        };
    }

    static int computeWorkedMinutes(String logInText, String logOutText) {
        LocalTime logIn = parseTime(logInText);
        LocalTime logOut = parseTime(logOutText);

        if (logIn == null || logOut == null) {
            return 0;
        }

        LocalTime effectiveIn = logIn;
        if (!logIn.isAfter(GRACE_LIMIT)) {
            effectiveIn = WORK_START;
        } else if (logIn.isBefore(WORK_START)) {
            effectiveIn = WORK_START;
        }

        LocalTime effectiveOut = logOut.isAfter(WORK_END) ? WORK_END : logOut;

        if (!effectiveOut.isAfter(effectiveIn)) {
            return 0;
        }

        long totalMinutes = Duration.between(effectiveIn, effectiveOut).toMinutes();
        if (totalMinutes > 60) {
            totalMinutes -= 60;
        } else {
            totalMinutes = 0;
        }

        if (totalMinutes > 480) {
            totalMinutes = 480;
        }

        return (int) totalMinutes;
    }

    static BigDecimal minutesToHours(int minutes) {
        return new BigDecimal(minutes).divide(SIXTY, 15, RoundingMode.HALF_UP);
    }

    static BigDecimal minutesToAmount(int minutes, BigDecimal hourlyRate) {
        return hourlyRate.multiply(new BigDecimal(minutes)).divide(SIXTY, 15, RoundingMode.HALF_UP);
    }

    static BigDecimal computeSss(BigDecimal combinedGross) {
        if (combinedGross.compareTo(ZERO) <= 0) {
            return ZERO;
        }

        if (combinedGross.compareTo(SSS_MIN_SALARY) < 0) {
            return new BigDecimal("135");
        }

        if (combinedGross.compareTo(SSS_MAX_SALARY) >= 0) {
            return new BigDecimal("1125");
        }

        BigDecimal increments = combinedGross.subtract(SSS_MIN_SALARY).divideToIntegralValue(SSS_STEP);
        return SSS_BASE.add(SSS_INCREMENT.multiply(increments.add(BigDecimal.ONE)));
    }

    static BigDecimal computePhilHealth(BigDecimal combinedGross) {
        if (combinedGross.compareTo(ZERO) <= 0) {
            return ZERO;
        }

        if (combinedGross.compareTo(new BigDecimal("10000")) <= 0) {
            return new BigDecimal("150");
        }

        if (combinedGross.compareTo(new BigDecimal("60000")) < 0) {
            return combinedGross.multiply(new BigDecimal("0.03")).divide(new BigDecimal("2"), 15, RoundingMode.HALF_UP);
        }

        return new BigDecimal("900");
    }

    static BigDecimal computePagIbig(BigDecimal combinedGross) {
        if (combinedGross.compareTo(ZERO) <= 0) {
            return ZERO;
        }

        BigDecimal contribution;

        if (combinedGross.compareTo(new BigDecimal("1000")) < 0) {
            contribution = ZERO;
        } else if (combinedGross.compareTo(new BigDecimal("1500")) <= 0) {
            contribution = combinedGross.multiply(new BigDecimal("0.01"));
        } else {
            contribution = combinedGross.multiply(new BigDecimal("0.02"));
        }

        if (contribution.compareTo(new BigDecimal("100")) > 0) {
            contribution = new BigDecimal("100");
        }

        return contribution;
    }

    static BigDecimal computeWithholdingTax(BigDecimal taxableIncome) {
        if (taxableIncome.compareTo(new BigDecimal("20832")) <= 0) {
            return ZERO;
        }

        if (taxableIncome.compareTo(new BigDecimal("33333")) <= 0) {
            return taxableIncome.subtract(new BigDecimal("20833")).multiply(new BigDecimal("0.20"));
        }

        if (taxableIncome.compareTo(new BigDecimal("66667")) <= 0) {
            return new BigDecimal("2500").add(
                taxableIncome.subtract(new BigDecimal("33333")).multiply(new BigDecimal("0.25"))
            );
        }

        if (taxableIncome.compareTo(new BigDecimal("166667")) <= 0) {
            return new BigDecimal("10833").add(
                taxableIncome.subtract(new BigDecimal("66667")).multiply(new BigDecimal("0.30"))
            );
        }

        if (taxableIncome.compareTo(new BigDecimal("666667")) <= 0) {
            return new BigDecimal("40833.33").add(
                taxableIncome.subtract(new BigDecimal("166667")).multiply(new BigDecimal("0.32"))
            );
        }

        return new BigDecimal("200833.33").add(
            taxableIncome.subtract(new BigDecimal("666667")).multiply(new BigDecimal("0.35"))
        );
    }

    static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    static LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value.trim(), TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    static String[] splitCsv(String line) {
        String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (int index = 0; index < values.length; index++) {
            values[index] = values[index].replace("\"", "").trim();
        }
        return values;
    }

    static BigDecimal parseAmount(String value) {
        return new BigDecimal(value.replace(",", "").trim());
    }

    static String buildEmployeeName(String[] employee) {
        return employee[2] + " " + employee[1];
    }

    static String monthName(int month) {
        switch (month) {
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return "Month";
        }
    }

    static BigDecimal normalize(BigDecimal value) {
        return value.stripTrailingZeros();
    }

    static String formatAmount(BigDecimal value) {
        return value.setScale(2, RoundingMode.DOWN).toPlainString();
    }

    static void printSeparator(String character) {
        System.out.println(character.repeat(48));
    }
}

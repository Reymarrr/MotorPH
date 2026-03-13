import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class MotorPH {
    // dito yung mga file path
    static final String EMPLOYEE_FILE = "Employee Details.csv";
    static final String ATTENDANCE_FILE = "Attendance Record.csv";

    // dito yung mga date format identifier
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");
    static final LocalTime START_TIME   = LocalTime.of(8, 0);
    static final LocalTime GRACE_PERIOD = LocalTime.of(8, 10);
    static final LocalTime CUTOFF_TIME  = LocalTime.of(17, 0);

    // password para sa account
    static final String PASSWORD = "12345";

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("=================================");
        System.out.println("        MOTORPH PAYROLL");
        System.out.println("=================================");
        System.out.println("Enter your credentials\n");
        System.out.print("Username: ");
        String username = input.nextLine();
        System.out.print("Password: ");
        String password = input.nextLine();

       if (!username.equals("employee") || !password.equals(PASSWORD)) {
            System.out.println("\nIncorrect username and/or password. Program ends.");
            input.close();
            return;
        }

        while (true) {
            System.out.println("\n=== EMPLOYEE MENU ===");
            System.out.println("1. View Employee Info");
            System.out.println("2. Compute Payroll");
            System.out.println("3. Exit Program");
            System.out.print("Choose 1, 2, or 3: ");

            int menuChoice = input.nextInt();
            input.nextLine();

            if (menuChoice == 1) {

                System.out.print("Enter Employee Number: ");
                String empNumToFind = input.nextLine();
                showEmployeeInfo(empNumToFind);

            } 
            else if (menuChoice == 2) {

                System.out.print("Enter Employee Number: ");
                String empNumToFind = input.nextLine();

                System.out.print("Enter month number (1-12): ");
                int month = Integer.parseInt(input.nextLine());

                computeAndDisplayPayroll(empNumToFind, month);

            } 
            else if (menuChoice == 3) {

                System.out.println("Exiting program.");
                break;

            } 
            else {
                System.out.println("Invalid choice.");
            }
        }

        input.close();
    }

    //=======================================
    // SHOW EMPLOYEE INFO
    //=======================================
    public static void showEmployeeInfo(String empNumToFind) {
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] empData = splitCSV(line);

                if (empData.length >= 4 && empData[0].equals(empNumToFind)) {
                    System.out.println("\n--- FOUND! ---");
                    System.out.println("Employee Number: " + empData[0]);
                    System.out.println("Employee Name: " + empData[2] + " " + empData[1]);
                    System.out.println("Birthday: " + empData[3]);
                    found = true;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        if (!found) {
            System.out.println("Employee number does not exist.");
        }
    }

    //=======================================
    // COMPUTE AND DISPLAY PAYROLL
    //=======================================
    public static void computeAndDisplayPayroll(String empNumToFind, int month) {
        boolean found = false;
        String lastName = "";
        String firstName = "";
        String birthday = "";
        double hourlyRate = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] empData = splitCSV(line);

                // column 18 = hourly rate
                if (empData.length > 18 && empData[0].equals(empNumToFind)) {
                    lastName = empData[1];
                    firstName = empData[2];
                    birthday = empData[3];
                    hourlyRate = Double.parseDouble(empData[18].replace(",", ""));
                    found = true;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading employee file: " + e.getMessage());
            return;
        }

        if (!found) {
            System.out.println("Employee number does not exist.");
            return;
        }

        double[] payData = computeGrossPay(empNumToFind, hourlyRate, month);

        System.out.println("\n========== PAYROLL ==========");
        System.out.println("Employee Number    : " + empNumToFind);
        System.out.println("Employee Name      : " + firstName + " " + lastName);
        System.out.println("Birthday           : " + birthday);
        System.out.println("Month              : " + YearMonth.of(2024, month).getMonth());
        System.out.printf("Hourly Rate        : PHP%.2f%n", hourlyRate);
        System.out.println("----------------------------------");
        System.out.printf("Hours 1st Cutoff   : %.2f%n", payData[0]);
        System.out.printf("Hours 2nd Cutoff   : %.2f%n", payData[1]);
        System.out.printf("Gross 1st Cutoff   : PHP%.2f%n", payData[2]);
        System.out.printf("Gross 2nd Cutoff   : PHP%.2f%n", payData[3]);
        System.out.println("----------------------------------");
        System.out.printf("SSS                : PHP%.2f%n", payData[4]);
        System.out.printf("PhilHealth         : PHP%.2f%n", payData[5]);
        System.out.printf("Pag-IBIG           : PHP%.2f%n", payData[6]);
        System.out.printf("Tax                : PHP%.2f%n", payData[7]);
        System.out.printf("Total Deductions   : PHP%.2f%n", payData[8]);
        System.out.printf("Net Pay 2nd Cutoff : PHP%.2f%n", payData[9]);
        System.out.printf("Days Worked        : %.0f%n", payData[10]);
    }

    //=======================================
    // COMPUTE GROSS PAY, returns double[]
    // [0] hoursFirstCutoff
    // [1] hoursSecondCutoff
    // [2] grossFirst
    // [3] grossSecond
    // [4] sss
    // [5] philHealth
    // [6] pagIbig
    // [7] tax
    // [8] totalDeductions
    // [9] netPaySecond
    // [10] daysInMonth
    //=======================================
    public static double[] computeGrossPay(String empNumToFind, double hourlyRate, int month) {
        double hoursFirstCutoff = 0;
        double hoursSecondCutoff = 0;
        double grossFirst = 0;
        double grossSecond = 0;
        double sss = 0;
        double philHealth = 0;
        double pagIbig = 0;
        double tax = 0;
        double totalDeductions = 0;
        double netPaySecond = 0;
        double daysInMonth = 0;

        // extra helper values
        double lateDeductionFirst = 0;
        double lateDeductionSecond = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(ATTENDANCE_FILE))) {
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] attData = splitCSV(line);

                // Example assumption:
                // [0] employee number
                // [3] date
                // [4] time in
                // [5] time out
                if (attData.length > 5 && attData[0].equals(empNumToFind)) {
                    String dateText = attData[3];
                    int recordMonth = extractMonth(dateText);
                    int recordDay = extractDay(dateText);

                    if (recordMonth == month) {
                        LocalTime timeIn = LocalTime.parse(attData[4], TIME_FORMAT);
                        LocalTime timeOut = LocalTime.parse(attData[5], TIME_FORMAT);

                        // if employee logs out beyond cutoff, cap to 5:00 PM
                        if (timeOut.isAfter(CUTOFF_TIME)) {
                            timeOut = CUTOFF_TIME;
                        }

                        // avoid negative worked hours
                        if (timeOut.isBefore(timeIn)) {
                            continue;
                        }

                        double workedHours = Duration.between(timeIn, timeOut).toMinutes() / 60.0;
                        double lateHours = 0;

                        // compute late only if beyond grace period
                        if (timeIn.isAfter(GRACE_PERIOD)) {
                            lateHours = Duration.between(START_TIME, timeIn).toMinutes() / 60.0;
                        }

                        double lateDeduction = lateHours * hourlyRate;
                        daysInMonth++;

                        if (recordDay >= 1 && recordDay <= 15) {
                            hoursFirstCutoff += workedHours;
                            lateDeductionFirst += lateDeduction;
                        } else if (recordDay >= 16) {
                            hoursSecondCutoff += workedHours;
                            lateDeductionSecond += lateDeduction;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
        }

        grossFirst = (hoursFirstCutoff * hourlyRate) - lateDeductionFirst;
        grossSecond = (hoursSecondCutoff * hourlyRate) - lateDeductionSecond;

        if (grossFirst < 0) grossFirst = 0;
        if (grossSecond < 0) grossSecond = 0;

        // monthly gross after late deductions
        double monthlyGross = grossFirst + grossSecond;

        // deductions based on total monthly gross
        sss = computeSSS(monthlyGross);
        philHealth = computePhilHealth(monthlyGross);
        pagIbig = computePagibig(monthlyGross);
        tax = computeTax(monthlyGross);

        totalDeductions = sss + philHealth + pagIbig + tax;

        // deductions applied to 2nd cutoff
        netPaySecond = grossSecond - totalDeductions;

        return new double[] {
            hoursFirstCutoff,
            hoursSecondCutoff,
            grossFirst,
            grossSecond,
            sss,
            philHealth,
            pagIbig,
            tax,
            totalDeductions,
            netPaySecond,
            daysInMonth
        };
    }

    //=======================================
    // CONTRIBS DEDUCTION METHODS
    //=======================================
    public static double computeSSS(double salary) {
        if (salary < 3250) {
            return 135.00;
        } else if (salary < 3750) {
            return 157.50;
        } else if (salary < 4250) {
            return 180.00;
        } else if (salary < 4750) {
            return 202.50;
        } else if (salary < 5250) {
            return 225.00;
        } else if (salary < 5750) {
            return 247.50;
        } else if (salary < 6250) {
            return 270.00;
        } else if (salary < 6750) {
            return 292.50;
        } else if (salary < 7250) {
            return 315.00;
        } else if (salary < 7750) {
            return 337.50;
        } else if (salary < 8250) {
            return 360.00;
        } else if (salary < 8750) {
            return 382.50;
        } else if (salary < 9250) {
            return 405.00;
        } else if (salary < 9750) {
            return 427.50;
        } else if (salary < 10250) {
            return 450.00;
        } else if (salary < 10750) {
            return 472.50;
        } else if (salary < 11250) {
            return 495.00;
        } else if (salary < 11750) {
            return 517.50;
        } else if (salary < 12250) {
            return 540.00;
        } else if (salary < 12750) {
            return 562.50;
        } else if (salary < 13250) {
            return 585.00;
        } else if (salary < 13750) {
            return 607.50;
        } else if (salary < 14250) {
            return 630.00;
        } else if (salary < 14750) {
            return 652.50;
        } else if (salary < 15250) {
            return 675.00;
        } else if (salary < 15750) {
            return 697.50;
        } else if (salary < 16250) {
            return 720.00;
        } else if (salary < 16750) {
            return 742.50;
        } else if (salary < 17250) {
            return 765.00;
        } else if (salary < 17750) {
            return 787.50;
        } else if (salary < 18250) {
            return 810.00;
        } else if (salary < 18750) {
            return 832.50;
        } else if (salary < 19250) {
            return 855.00;
        } else if (salary < 19750) {
            return 877.50;
        } else if (salary < 20250) {
            return 900.00;
        } else if (salary < 20750) {
            return 922.50;
        } else if (salary < 21250) {
            return 945.00;
        } else if (salary < 21750) {
            return 967.50;
        } else if (salary < 22250) {
            return 990.00;
        } else if (salary < 22750) {
            return 1012.50;
        } else if (salary < 23250) {
            return 1035.00;
        } else if (salary < 23750) {
            return 1057.50;
        } else if (salary < 24250) {
            return 1080.00;
        } else if (salary < 24750) {
            return 1102.50;
        } else {
            return 1125.00;
        }
    }

    public static double computePhilHealth(double salary) {

        double premium;

        if (salary <= 10000) {
            premium = 300;
        } 
        else if (salary < 60000) {
            premium = salary * 0.03;
        } 
        else {
            premium = 1800;
        }

        // employee share is half
        return premium / 2;
    }

    public static double computePagibig(double salary) {

        double contribution;

        if (salary >= 1000 && salary <= 1500) {
            contribution = salary * 0.01;
        } else {
            contribution = salary * 0.02;
        }

        // Pag-IBIG employee contribution is capped at 100
        if (contribution > 100) {
            contribution = 100;
        }

        return contribution;
    }

    public static double computeTax(double salary) {

        if (salary <= 20832) {
            return 0;
        }
        else if (salary < 33333) {
            return (salary - 20833) * 0.20;
        }
        else if (salary < 66667) {
            return 2500 + ((salary - 33333) * 0.25);
        }
        else if (salary < 166667) {
            return 10833 + ((salary - 66667) * 0.30);
        }
        else if (salary < 666667) {
            return 40833.33 + ((salary - 166667) * 0.32);
        }
        else {
            return 200833.33 + ((salary - 666667) * 0.35);
        }
    }

    //==============================================================================
    // HELPERS - code block para ma-read yung data sa csv, kasama yung text splitter
    //==============================================================================
    public static String[] splitCSV(String line) {
        String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (int i = 0; i < data.length; i++) {
            data[i] = data[i].replace("\"", "").trim();
        }
        return data;
    }

    public static int extractMonth(String dateText) {
        if (dateText.contains("-")) {
            String[] parts = dateText.split("-");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        } else if (dateText.contains("/")) {
            String[] parts = dateText.split("/");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[0]);
            }
        }
        return -1;
    }

    public static int extractDay(String dateText) {
        if (dateText.contains("-")) {
            String[] parts = dateText.split("-");
            if (parts.length >= 3) {
                return Integer.parseInt(parts[2]);
            }
        } else if (dateText.contains("/")) {
            String[] parts = dateText.split("/");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        }
        return -1;
    }
}
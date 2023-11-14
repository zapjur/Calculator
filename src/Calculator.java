import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Stack;

public class Calculator extends JFrame implements ActionListener {

    private JTextField resultField;
    private JTextField equationField;
    private JPanel panel;
    private String equation = "";
    private String lastOperation = "";
    private boolean firstNum = true;
    private boolean errorOccured = false;
    private boolean lastEqual = false;
    private static final Color bgColor = new Color(52, 52, 52);
    private static final Color fgColor = new Color(255, 255, 255);

    public Calculator() {
        setTitle("Calculator");
        setSize(270, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(bgColor);

        resultField = new JTextField("0");
        resultField.setPreferredSize(new Dimension(270, 50));
        resultField.setFont(new Font("Arial", Font.PLAIN, 28));
        resultField.setEditable(false);
        resultField.setHorizontalAlignment(JTextField.RIGHT);
        resultField.setBackground(bgColor);
        resultField.setForeground(fgColor);

        equationField = new JTextField();
        equationField.setPreferredSize(new Dimension(270, 50));
        equationField.setFont(new Font("Arial", Font.PLAIN, 18));
        equationField.setEditable(false);
        equationField.setHorizontalAlignment(JTextField.RIGHT);
        equationField.setBackground(bgColor);
        equationField.setForeground(fgColor);

        panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);

        String[] buttonLabels = {
                "C", "+/-", "%", "/",
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "0", ".", "="
        };

        int j = 0;
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setPreferredSize(new Dimension(60, 40));
            button.setFont(label.equals("+/-") ? new Font("Arial", Font.PLAIN, 13) : new Font("Arial", Font.PLAIN, 18));
            button.setFocusPainted(false);
            button.addActionListener(this);
            button.addMouseListener(new ButtonListener(button, label));

            gbc.gridx = j % 4;
            gbc.gridy = j / 4;

            if (label.equals("=")) {
                gbc.gridwidth = 2;
                button.setUI(new FuncButtonUI());
            } else {
                gbc.gridwidth = 1;
                button.setUI(new NumberButtonUI());

                if (label.matches(".*\\d.*") || label.equals(".")) {
                    button.setUI(new NumberButtonUI());
                } else {
                    button.setUI(new FuncButtonUI());
                }
            }

            panel.add(button, gbc);
            j++;
        }

        add(equationField, BorderLayout.NORTH);
        add(resultField, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        String buttonText = source.getText();

        switch (buttonText) {
            case "C":
                lastEqual = false;
                clearCalculator();
                firstNum = true;
                errorOccured = false;
                resultField.setFont(new Font("Arial", Font.PLAIN, 28));
                break;
            case "=":
                if(errorOccured) return;
                if(firstNum) return;
                if(lastEqual) {
                    lastEqual = true;
                    equation += lastOperation;
                    equationField.setText(equation);
                    resultField.setText("");
                    calculateResult();
                }
                else if(isOperator(equation.charAt(equation.length()-1))) {

                }
                else {
                    lastEqual = true;
                    resultField.setText("");
                    calculateResult();
                    if(errorOccured) return;
                    lastOperation = getLastOperation();
                    equation = resultField.getText();
                    equationField.setText(equation);
                }

                break;
            case "+/-":
                lastEqual = false;
                if(errorOccured) return;
                changeSign();
                break;
            case "+":
            case "-":
            case "*":
            case "/":
                lastEqual = false;
                if(firstNum) return;
                if(errorOccured) return;
                equation += buttonText;
                equationField.setText(equation);
                resultField.setText("");
                break;
            default:
                lastEqual = false;
                if(errorOccured) return;
                if(firstNum && !resultField.getText().equals("-")){
                    resultField.setText("");
                    firstNum = false;
                }
                else if(firstNum && resultField.getText().equals("-")){
                    firstNum = false;
                }
                equation += buttonText;
                resultField.setText(resultField.getText() + buttonText);
                break;
        }
    }

    private void clearCalculator() {
        equation = "";
        resultField.setText("");
        equationField.setText(equation);
        resultField.setText("0");
    }

    private void calculateResult() {
        if (!equation.isEmpty()) {
            try {
                double result = evaluateExpression();
                resultField.setText(resultField.getText() + result);
            }catch(ArithmeticException e){
                resultField.setFont(new Font("Arial", Font.PLAIN, 20));
                resultField.setText("ERROR: Division by zero");
                equationField.setText("");
                errorOccured = true;

            } catch (Exception e) {
                resultField.setText("Error");
                errorOccured = true;
            }
        }
    }
    private double evaluateExpression() {
        String postfix = infixToPostfix(equation);
        return evaluatePostfix(postfix);
    }

    private void changeSign() {
        if (!equation.isEmpty()) {
            char lastChar = equation.charAt(equation.length() - 1);

            if (Character.isDigit(lastChar) || lastChar == '.') {
                int lastIndex = equation.length() - 1;
                while (lastIndex >= 0 && (Character.isDigit(equation.charAt(lastIndex)) || equation.charAt(lastIndex) == '.')) {
                    lastIndex--;
                }

                if (lastIndex >= 0 && isOperator(equation.charAt(lastIndex))) {
                    equation = equation.substring(0, lastIndex + 1) + "-" + equation.substring(lastIndex + 1);
                } else {
                    equation = equation.substring(0, equation.length() - 1) + "-" + lastChar;
                }
            } else if (isOperator(lastChar)) {
                equation += "-";
            }

            resultField.setText(resultField.getText().startsWith("-") ? resultField.getText().substring(1) : "-" + resultField.getText());
        }
        else{
            equation += "-";
            resultField.setText("-");
        }
    }

    private String getLastOperation() {
        char lastChar = equation.charAt(equation.length() - 1);
        if (Character.isDigit(lastChar) || lastChar == '.') {
            int lastIndex = equation.length() - 1;
            while (lastIndex >= 0 && (Character.isDigit(equation.charAt(lastIndex)) || equation.charAt(lastIndex) == '.')) {
                lastIndex--;
            }
            if (lastIndex >= 0 && isOperator(equation.charAt(lastIndex))) {
                return equation.substring(lastIndex);
            }
        }
        return "";
    }

    private String infixToPostfix(String infix) {
        StringBuilder postfix = new StringBuilder();
        Stack<Character> operatorStack = new Stack<>();
        boolean negativeNumber = false;

        for (char c : infix.toCharArray()) {
            if (Character.isDigit(c) || c == '.') {
                if (negativeNumber) {
                    postfix.append("-");
                    negativeNumber = false;
                }
                postfix.append(c);
            } else if (isOperator(c)) {
                if (c == '-' && (postfix.isEmpty() || !Character.isDigit(postfix.charAt(postfix.length() - 1)))) {
                    negativeNumber = true;
                } else {
                    postfix.append(" ");
                    while (!operatorStack.isEmpty() && hasHigherPrecedence(operatorStack.peek(), c)) {
                        postfix.append(operatorStack.pop()).append(" ");
                    }
                    operatorStack.push(c);
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            postfix.append(" ").append(operatorStack.pop()).append(" ");
        }

        return postfix.toString();
    }

    private double evaluatePostfix(String postfix) throws ArithmeticException {
        Stack<Double> operandStack = new Stack<>();
        String[] tokens = postfix.split("\\s+");

        for (String token : tokens) {
            if (token.matches("-?\\d+\\.?\\d*")) {
                operandStack.push(Double.parseDouble(token));
            } else if (isOperator(token.charAt(0))) {
                double operand2 = operandStack.pop();
                double operand1 = operandStack.pop();
                double result = performOperation(operand1, operand2, token.charAt(0));

                operandStack.push(result);
            }
        }

        return operandStack.pop();
    }


    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean hasHigherPrecedence(char op1, char op2) {
        int prec1 = getPrecedence(op1);
        int prec2 = getPrecedence(op2);

        return prec1 >= prec2;
    }

    private int getPrecedence(char operator) {
        return switch (operator) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            default -> 0;
        };
    }

    private double performOperation(double operand1, double operand2, char operator){
        return switch (operator) {
            case '+' -> operand1 + operand2;
            case '-' -> operand1 - operand2;
            case '*' -> operand1 * operand2;
            case '/' -> {
                if (operand2 != 0) {
                    yield operand1 / operand2;
                } else {
                    throw new ArithmeticException();
                }
            }
            default -> throw new IllegalArgumentException("Invalid operator");
        };
    }

    private static class NumberButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        public void paint(Graphics g, JComponent c) {
            g.setColor(new Color(100, 100, 100));
            c.setForeground(fgColor);
            c.setBackground(bgColor);
            g.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
            super.paint(g, c);
        }
    }

    private static class NumberButtonHoveredUI extends javax.swing.plaf.basic.BasicButtonUI {
        public void paint(Graphics g, JComponent c) {
            g.setColor(new Color(120, 120, 120));
            c.setForeground(fgColor);
            c.setBackground(bgColor);
            g.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
            super.paint(g, c);
        }
    }

    private static class NumberButtonPressedUI extends javax.swing.plaf.basic.BasicButtonUI {
        public void paint(Graphics g, JComponent c) {
            g.setColor(new Color(140, 140, 140));
            c.setForeground(fgColor);
            c.setBackground(bgColor);
            g.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
            super.paint(g, c);
        }
    }

    private static class FuncButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        public void paint(Graphics g, JComponent c) {
            g.setColor(new Color(240, 165, 0));
            c.setForeground(fgColor);
            c.setBackground(bgColor);
            g.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
            super.paint(g, c);
        }
    }

    private static class FuncButtonHoveredUI extends javax.swing.plaf.basic.BasicButtonUI {
        public void paint(Graphics g, JComponent c) {
            g.setColor(new Color(240, 190, 50));
            c.setForeground(fgColor);
            c.setBackground(bgColor);
            g.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
            super.paint(g, c);
        }
    }

    private static class FuncButtonPressedUI extends javax.swing.plaf.basic.BasicButtonUI {
        public void paint(Graphics g, JComponent c) {
            g.setColor(new Color(245, 205, 65));
            c.setForeground(fgColor);
            c.setBackground(bgColor);
            g.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
            super.paint(g, c);
        }
    }

    private record ButtonListener(JButton button, String label) implements MouseListener {

        public void mousePressed(MouseEvent e) {
            if (label.matches(".*\\d.*") || label.equals(".")) {
                button.setUI(new NumberButtonPressedUI());
            } else {
                button.setUI(new FuncButtonPressedUI());
            }
        }

        public void mouseClicked(MouseEvent e) {

        }

        public void mouseReleased(MouseEvent e) {
            if (label.matches(".*\\d.*") || label.equals(".")) {
                button.setUI(new NumberButtonUI());
            } else {
                button.setUI(new FuncButtonUI());
            }
        }

        public void mouseEntered(MouseEvent e) {
            if (label.matches(".*\\d.*") || label.equals(".")) {
                button.setUI(new NumberButtonHoveredUI());
            } else {
                button.setUI(new FuncButtonHoveredUI());
            }
        }

        public void mouseExited(MouseEvent e) {
            if (label.matches(".*\\d.*") || label.equals(".")) {
                button.setUI(new NumberButtonUI());
            } else {
                button.setUI(new FuncButtonUI());
            }
        }
    }

    public static void main(String[] args) {
        new Calculator();
    }
}
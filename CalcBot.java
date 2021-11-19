import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Stack;

public class CalcBot extends Bot {

    String nickname = null;
    String channel = null;

    public CalcBot(String ip, Integer port, String nick, String channel) throws Exception {
        super(ip, port);
        this.nickname = nick;
        this.channel = channel;
        fd = (SSLSocket) SSLSocketFactory.getDefault().createSocket(address, port);
        input = new BufferedReader(new InputStreamReader(fd.getInputStream()));
        output = new BufferedWriter(new OutputStreamWriter(fd.getOutputStream()));
    }

    public static void main(String[] args) throws Exception {
        CalcBot bot = new CalcBot("irc.xolus.net", 6697, "calcBot", "#n-pn");
        bot.connect();
        bot.run();
    }

    public void connect() {
        try {

            String recv;

            output.write("NICK " + this.nickname + "\r\n");
            output.write("USER " + this.nickname + " " + this.nickname + ": Java bot\r\n");
            output.flush();

            while ((recv = input.readLine()) != null) {
                System.out.println(recv);
                if (recv.contains("You are connected")) {
                    output.write("JOIN " + this.channel + "\r\n");
                    output.flush();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int precedence(char op) {
        switch (op) {
            case '^':
                return 4;
            case '*':
                return 3;
            case '/':
                return 3;
            case '%':
                return 2;
            case '-':
                return 1;
            case '–':
                return 1;
            case '+':
                return 1;
            case '#':
                return 4;
        }
        return 0;
    }

    public boolean right_asso(char op) {
        if (op == '^')
            return true;
        return false;
    }

    public boolean isUnaryMinus(char[] tokens, int i) {
        if (tokens[i] == '-' || tokens[i] == '–') {
            if (i - 1 < 0 || tokens[i - 1] == '(' || this.isOperator(tokens[i - 1])) {
                return true;
            }
            return false;
        }
        return false;
    }

    public double compute(char op, double x, double y){
        switch (op) {
            case '+':
                return y + x;
            case '-':
                return y - x;
            case '–':
                return y - x;
            case '*':
                return y * x;
            case '/':
                return y / x;
            case '÷':
                return y / x;
            case '%':
                return y % x;
            case '^':
                return Math.pow(y, x);
            case '#':
                return x * y; // * -1 to negate
        }
        return -1;
    }

    public boolean isOperator(char op) {
        if (op == '*')
            return true;
        if (op == '/')
            return true;
        if (op == '-')
            return true;
        if(op == '–')
            return true;
        if (op == '+')
            return true;
        if (op == '^')
            return true;
        if(op == '÷')
            return true;
        if(op == '%')
            return true;
        if (op == '#')
            return true;

        return false;
    }

    public boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public String shunt_yard(String calc) throws Exception{
        char[] tokens = calc.trim().toCharArray();

        Stack<Double> out = new Stack<Double>();
        Stack<Character> operator = new Stack<Character>();
        try {
            for (int i = 0; i < tokens.length; i++) {

                if (this.isDigit(tokens[i])) {
                    StringBuilder temp = new StringBuilder();
                    temp.append(tokens[i]);
                    int j = i + 1;
                    while (j < tokens.length && this.isDigit(tokens[j])) {
                        temp.append(tokens[j]);
                        j++;
                    }
                    j--;
                    i = j;
                    out.push(Double.parseDouble(temp.toString()));
                    continue;
                } else if (tokens[i] == '(') {
                    operator.push(tokens[i]);
                    continue;
                } else if (tokens[i] == ')') {
                    while (operator.peek() != '(') {
                        if (operator.peek() == '#')
                            out.push(this.compute(operator.pop(), out.pop(), -1));
                        else
                            out.push(this.compute(operator.pop(), out.pop(), out.pop()));
                    }
                    operator.pop();
                    continue;
                } else if (this.isUnaryMinus(tokens, i)) {
                    operator.push('#');
                    continue;
                } else if (this.isOperator(tokens[i])) {
                    while (!operator.empty() && operator.peek() != '(' && this.precedence(tokens[i]) <= this.precedence(operator.peek()) && !this.right_asso(tokens[i])) {
                        if (operator.peek() == '#')
                            out.push(this.compute(operator.pop(), out.pop(), -1));
                        else
                            out.push(this.compute(operator.pop(), out.pop(), out.pop()));
                    }

                    operator.push(tokens[i]);
                    continue;
                }

            }
            while (!operator.empty()) {
                if (operator.peek() == '#')
                    out.push(this.compute(operator.pop(), out.pop(), -1));
                else
                    out.push(this.compute(operator.pop(), out.pop(), out.pop()));
            }
        }catch (Exception e){
            output.write("PRIVMSG " + this.channel + " " + e.toString()+ " \r\n");
            output.flush();
        }
        return out.toString();
    }

    public void run() throws Exception {
        String recv;
        while (true) {
            while ((recv = input.readLine()) != null) {

                System.out.println(recv);

                if (recv.contains(".calc")) {
                    output.write("PRIVMSG " + this.channel + " " + this.shunt_yard(recv.split(".calc")[1]) + " \r\n");
                    output.flush();
                    System.out.println(":PRIVMSG " + this.channel + " " + this.shunt_yard(recv.split(".calc")[1]) + " \r\n");
                }

                if (recv.startsWith("PING")) {
                    output.write("PONG " + ":" + recv.split(":")[1] + " \r\n");
                    output.flush();
                    System.out.println(":PONG " + ":" + recv.split(":")[1] + " \r\n");
                }
                if (recv.contains(".exit")) {
                    output.write("PRIVMSG " + this.channel + " exit() command called. Bye. \r\n");
                    output.flush();
                    System.out.println("EXITING command called.");
                    System.exit(0);
                }
            }
        }
    }
}

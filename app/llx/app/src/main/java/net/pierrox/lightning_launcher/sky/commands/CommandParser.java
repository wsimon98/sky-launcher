/*
MIT License

Copyright (c) 2026 Sky Launcher contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package net.pierrox.lightning_launcher.sky.commands;

/**
 * Splits a palette input line into a command token and an argument.
 * `:edit`            -> (":edit", "")
 * `.app signal`      -> (".app", "signal")
 * Anything not starting with ':' or '.' is not a command.
 */
public class CommandParser {
    public final String token;
    public final String argument;

    private CommandParser(String token, String argument) {
        this.token = token;
        this.argument = argument;
    }

    public static CommandParser parse(String input) {
        if (input == null) return null;
        String s = input.trim();
        if (s.length() < 2) return null;
        char c = s.charAt(0);
        if (c != ':' && c != '.') return null;
        int space = s.indexOf(' ');
        if (space < 0) {
            return new CommandParser(s, "");
        }
        return new CommandParser(s.substring(0, space), s.substring(space + 1).trim());
    }
}

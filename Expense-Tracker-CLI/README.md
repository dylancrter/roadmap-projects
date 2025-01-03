# Expense-Tracker-CLI

## How to Run
Open your terminal of choice and run the following commands
```bash
git clone https://github.com/dylancrter/Expense-Tracker-CLI
cd Expense-Tracker-CLI
go build expense-tracker.go
```
Lastly, run this command to run the application:
```bash
./expense-tracker <flags> <command>
```

## Commands
- add: adds an expense with specified description and amount
    - --description <string> (required)
    - --amount <int/float> (required)
- list: lists the user's expenses (id, date, description, amount)
- summary: adds up the user's expenses
    - --month <int> (optional)
- delete: deletes an expense with the specified id
    - --id <int> (required)

## Requirements
- Ensure that you have at least go version 1.16 installed
- This has only been tested on Linux and Unix-based systems

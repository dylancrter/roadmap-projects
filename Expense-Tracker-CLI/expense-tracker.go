package main

import (
	"encoding/json"
	"errors"
	"flag"
	"fmt"
	"io"
	"log"
	"os"
	"time"
)

type Expense struct {
	ID          int     `json:"id"`
	Date        string  `json:"date"`
	Description string  `json:"description"`
	Amount      float64 `json:"amount"`
}

type Expenses []Expense

func main() {
	var id = flag.Int("id", 0, "id of expense")
	var description = flag.String("description", "", "describes the expense")
	var amount = flag.Float64("amount", 0.0, "expense amount")
	var month = flag.Int("month", 0, "month of the expense")

	flag.Parse()

	args := flag.Args()
	if len(args) == 0 {
		fmt.Println("Please specify a command.\nUsage: ./expense-tracker <flags> <command>")
		return
	}

	var err error
	file, err := os.OpenFile("expenses.json", os.O_RDWR|os.O_CREATE, 0644)
	if err != nil {
		log.Fatal(err)
	}

	defer file.Close()

	switch args[0] {
	case "add":
		if err = add(file, description, amount); err != nil {
			log.Fatal(err)
		}
	case "list":
		if err = list(file); err != nil {
			log.Fatal(err)
		}
	case "summary":
		if err = summary(file, month); err != nil {
			log.Fatal(err)
		}
	case "delete":
		if err = deleteExpense(file, id); err != nil {
			log.Fatal(err)
		}
	}

	if err != nil {
		fmt.Printf(err.Error())
	}
}

func add(file *os.File, description *string, amount *float64) error {
	currentTime := time.Now()
	date := currentTime.Format("2006-01-02")

	if _, err := file.Seek(0, 0); err != nil {
		return fmt.Errorf("add: %v", err)
	}

	bytes, err := io.ReadAll(file)
	if err != nil {
		return fmt.Errorf("add: %v", err.Error())
	}

	var expenses Expenses
	if len(bytes) > 0 {
		if err := json.Unmarshal(bytes, &expenses); err != nil {
			return fmt.Errorf("add: %v", err)
		}
	}

	var id int
	if len(expenses) > 0 {
		id = expenses[len(expenses)-1].ID + 1
	} else {
		id = 1
	}
	newExpense := Expense{
		ID:          id,
		Description: *description,
		Amount:      *amount,
		Date:        date,
	}
	expenses = append(expenses, newExpense)

	updatedData, err := json.MarshalIndent(expenses, "", "  ")
	if err != nil {
		return fmt.Errorf("add: %v", err)
	}

	if err := file.Truncate(0); err != nil {
		return fmt.Errorf("add: %v", err)
	}
	if _, err := file.Seek(0, 0); err != nil {
		return fmt.Errorf("add: %v", err)
	}
	if _, err := file.Write(updatedData); err != nil {
		return fmt.Errorf("add: %v", err)
	}

	fmt.Println("Added expense:", newExpense)
	return nil
}

func list(file *os.File) error {
	if _, err := file.Seek(0, 0); err != nil {
		return fmt.Errorf("list: %v", err)
	}
	bytes, err := io.ReadAll(file)
	if err != nil {
		return fmt.Errorf("list: %v", err)
	}

	var expenses Expenses
	if len(bytes) > 0 {
		if err := json.Unmarshal(bytes, &expenses); err != nil {
			return fmt.Errorf("list: %v", err)
		}
	}

	for _, expense := range expenses {
		print := fmt.Sprintf("ID: %d, Date: %s, Descrition: %s, Amount: %.2f", expense.ID, expense.Date, expense.Description, expense.Amount)
		fmt.Println(print)
	}
	return nil
}

func summary(file *os.File, month *int) error {
	if *month < 0 || *month > 12 {
		err := errors.New("summary: invalid month")
		return err
	}

	if _, err := file.Seek(0, 0); err != nil {
		return fmt.Errorf("summary: %v", err)
	}
	bytes, err := io.ReadAll(file)
	if err != nil {
		return fmt.Errorf("summary: %v", err)
	}

	var expenses Expenses
	if len(bytes) > 0 {
		if err := json.Unmarshal(bytes, &expenses); err != nil {
			return fmt.Errorf("summary: %v", err)
		}
	}

	var total float64
	fmt.Println(*month)
	if *month == 0 {
		for _, expense := range expenses {
			total += expense.Amount
		}
	} else {
		for _, expense := range expenses {
			date, err := time.Parse("2006-01-02", expense.Date)
			if err != nil {
				return fmt.Errorf("summary: %v", err)
			}
			if *month == int(date.Month()) {
				total += expense.Amount
			}
		}
	}

	fmt.Println("Total Expenses: ", total)
	return nil
}

func deleteExpense(file *os.File, id *int) error {
	if *id < 0 {
		return errors.New("deleteExpense: please specify a valid id")
	}

	if _, err := file.Seek(0, 0); err != nil {
		return fmt.Errorf("deleteExpense: %v", err)
	}
	bytes, err := io.ReadAll(file)
	if err != nil {
		return fmt.Errorf("deleteExpense: %v", err)
	}

	var expenses Expenses
	if len(bytes) > 0 {
		if err := json.Unmarshal(bytes, &expenses); err != nil {
			return fmt.Errorf("deleteExpense: %v", err)
		}
	}

	index := -1
	for i, expense := range expenses {
		if expense.ID == *id {
			index = i
		}
	}
	if index == -1 {
		return errors.New("expense with specified id was not found")
	}

	removedExpense := expenses[index]
	expenses = append(expenses[:index], expenses[index+1:]...)

	updatedData, err := json.MarshalIndent(expenses, "", "  ")
	if err != nil {
		return fmt.Errorf("add: %v", err)
	}

	if err := file.Truncate(0); err != nil {
		return fmt.Errorf("add: %v", err)
	}
	if _, err := file.Seek(0, 0); err != nil {
		return fmt.Errorf("add: %v", err)
	}
	if _, err := file.Write(updatedData); err != nil {
		return fmt.Errorf("add: %v", err)
	}

	fmt.Println("Removed expense:", removedExpense)

	return nil
}

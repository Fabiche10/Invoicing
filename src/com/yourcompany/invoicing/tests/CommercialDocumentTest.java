package com.yourcompany.invoicing.tests;
 
// To use JPA
import static org.openxava.jpa.XPersistence.getManager;

import java.time.*;
import java.time.format.*;

import javax.persistence.*;

import org.openxava.tests.*;
 
abstract public class CommercialDocumentTest extends ModuleTestBase {
 
    private String number; // To store the number of the tested invoice
    private String model; // The model name to use in the query. Can be “Invoice” or “Order”

 
    public CommercialDocumentTest(
            String testName,
            String moduleName) // moduleName added as constructor argument
        {
    	super(testName, "Invoicing", moduleName);
        this.model = moduleName; // In this case module name matches model
        }
    
    public void testCreate() throws Exception {
        login("admin", "admin");
        calculateNumber(); // Added to calculate the next document number first
        verifyDefaultValues();
        chooseCustomer();
        addDetails();
        setOtherProperties();
        save();
        verifyEstimatedProfit(); // To test @Formula
        verifyCreated();
        remove();
    }
 
    private void verifyDefaultValues() throws Exception {
        execute("CRUD.new");
        assertValue("year", getCurrentYear());
        // assertValue("number", getNumber()); // Now number has no initial value
        assertValue("number", ""); // on create a new document
        assertValue("date", getCurrentDate());
    }
    
    private void chooseCustomer() throws Exception {
        setValue("customer.number", "1");
		assertValue("customer.name", "JAVIER PANIZA"); // The customer 1 should exist in DB
    }
 
    private void addDetails() throws Exception {
        assertCollectionRowCount("details", 0);

        // Before running this test code make sure
        //   product 1 has 19 as price, and 
        //   product 2 has 20 as price

        // Adding a detail line
        setValueInCollection("details", 0, "product.number", "1");
        assertValueInCollection("details", 0,
            "product.description", "Peopleware: Productive Projects and Teams");
        assertValueInCollection("details", 0,
            "pricePerUnit", "19.00"); // @DefaultValueCalculator
        setValueInCollection("details", 0, "quantity", "2");
        assertValueInCollection("details", 0,
            "amount", "38.00"); // Calculated property, section 'Simple calculated property'
     
        // Verifying total properties of collection
        assertTotalInCollection("details", 0, "amount", "38.00"); // Sum of amounts using +
        assertTotalInCollection("details", 1, "amount", "21.00"); // Default value from properties file
        assertTotalInCollection("details", 2, "amount", "7.98"); // VAT, with @Calculation
        assertTotalInCollection("details", 3, "amount", "45.98"); // Total amount, with @Calculation
     
        // Adding another detail
        setValueInCollection("details", 1, "product.number", "2");
        assertValueInCollection("details", 1, "product.description", "Arco iris de lágrimas");
        assertValueInCollection("details", 1, "pricePerUnit", "20.00"); 
        setValueInCollection("details", 1, "pricePerUnit", "10.00"); // Modifying the default value
        setValueInCollection("details", 1, "quantity", "1");
        assertValueInCollection("details", 1, "amount", "10.00");
     
        assertCollectionRowCount("details", 2); // Now we have 2 rows
     
        // Verifying total properties of collection
        assertTotalInCollection("details", 0, "amount", "48.00");
        assertTotalInCollection("details", 1, "amount", "21.00");
        assertTotalInCollection("details", 2, "amount", "10.08");
        assertTotalInCollection("details", 3, "amount", "58.08");
    }
    
    private void setOtherProperties() throws Exception {
        setValue("remarks", "This is a JUNIT test");
    }
 
    private void save() throws Exception {
        execute("CRUD.save");
        assertNoErrors();
        assertValue("customer.number", "");
        assertCollectionRowCount("details", 0);
        assertValue("remarks", "");
    }
 
    private void verifyCreated() throws Exception {
    	// setValue("year", getCurrentYear()); // We delete these lines
        // setValue("number", getNumber()); // for searching the document
        // execute("CRUD.refresh"); // because we already searched it with list mode
    	
        // In the rest of the test we assert that the values are the correct ones
        assertValue("year", getCurrentYear());
        assertValue("number", getNumber());
        assertValue("date", getCurrentDate());
        assertValue("customer.number", "1");
        assertValue("customer.name", "JAVIER PANIZA");
        assertCollectionRowCount("details", 2);
     
        // Row 0
        assertValueInCollection("details", 0, "product.number", "1");
        assertValueInCollection("details", 0, "product.description",
            "Peopleware: Productive Projects and Teams");
        assertValueInCollection("details", 0, "quantity", "2");
     
        // Row 1
        assertValueInCollection("details", 1, "product.number", "2");
        assertValueInCollection("details", 1, "product.description",
            "Arco iris de lágrimas");
        assertValueInCollection("details", 1, "quantity", "1");
        assertValue("remarks", "This is a JUNIT test");
    }
 
    private void remove() throws Exception {
        execute("CRUD.delete");
        assertNoErrors();
    }
 
    private String getCurrentYear() { // Current year in string format
        return Integer.toString(LocalDate.now().getYear()); // The standard
                                                        // way to do it with Java
    }
     
    private String getCurrentDate() { // Current date as string in short format
        return LocalDate.now().format( // The standard way to do it with Java
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
    }
     
    private void calculateNumber() {
        Query query = getManager().createQuery("select max(i.number) from " +
            model + // We change CommercialDocument for a variable
            " i where i.year = :year");
        query.setParameter("year", LocalDate.now().getYear());
        Integer lastNumber = (Integer) query.getSingleResult();
        if (lastNumber == null) lastNumber = 0;
        number = Integer.toString(lastNumber + 1);
    }
     
    private String getNumber() {
        return number;
    }
 
    private void verifyEstimatedProfit() throws Exception {
        execute("Mode.list"); // Changes to list mode
        setConditionValues(new String [] { // Filters to see in the list
            getCurrentYear(), getNumber() // only the newly created document
        });
        execute("List.filter"); // Does the filter
        assertValueInList(0, 0, getCurrentYear()); // Verifies that
        assertValueInList(0, 1, getNumber()); // the filter has worked
        assertValueInList(0, "estimatedProfit", "5.81"); // Asserts estimatedProfit
        execute("List.viewDetail", "row=0"); // Goes to detail mode
    }
}
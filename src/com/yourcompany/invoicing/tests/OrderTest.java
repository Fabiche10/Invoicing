package com.yourcompany.invoicing.tests;
 
public class OrderTest extends CommercialDocumentTest {
 
    public OrderTest(String testName) {
        super(testName, "Order");
    }
 
    public void testSetInvoice() throws Exception {
        login("admin", "admin");
        assertListNotEmpty(); // This test assumes that some orders already exist
        execute("List.viewDetail", "row=0"); // Goes to detail mode editing the first invoice
        execute("Sections.change", "activeSection=1"); // Changes to tab 1
        assertValue("invoice.number", ""); // This order has no
        assertValue("invoice.year", ""); // invoice assigned yet
        execute("Reference.search", // Clicks on the button to search the invoice, this
            "keyProperty=invoice.year"); // takes you to a list of invoices
        execute("List.orderBy", "property=number"); // To sort the list of invoices
        String year = getValueInList(0, "year"); // Stores the year and number of
        String number = getValueInList(0, "number"); // the first invoice in the list
        execute("ReferenceSearch.choose", "row=0"); // Chooses the first invoice
        assertValue("invoice.year", year); // On return to order detail we verify
        assertValue("invoice.number", number); // the invoice has been selected
    }
    
    public void testDeliveryDays() throws Exception {
        login("admin", "admin");
        assertListNotEmpty(); 
        execute("List.viewDetail", "row=0"); 
    	
        setValue("date", "6/5/20");
        assertValue("estimatedDeliveryDays", "1");
        setValue("date", "6/6/20");
        assertValue("estimatedDeliveryDays", "3");
        setValue("date", "6/7/20");
        assertValue("estimatedDeliveryDays", "2");
        execute("CRUD.save");
        execute("Mode.list"); // To verify that deliveryDays is synchronized
        assertValueInList(0, "deliveryDays", "2"); 

        execute("List.viewDetail", "row=0");
        setValue("date", "1/13/20");
        assertValue("estimatedDeliveryDays", "7");
        execute("CRUD.save");
        execute("Mode.list"); // To verify that deliveryDays is synchronized
        assertValueInList(0, "deliveryDays", "7");        
    }
    
}
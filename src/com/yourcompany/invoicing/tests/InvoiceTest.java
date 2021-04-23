package com.yourcompany.invoicing.tests;
 
public class InvoiceTest extends CommercialDocumentTest {
 
    public InvoiceTest(String testName) {
        super(testName, "Invoice");
    }
 
    public void testAddOrders() throws Exception {
        login("admin", "admin");
        assertListNotEmpty(); // This test assumes that some invoices already exist
        execute("List.orderBy", "property=number"); // To always work with the same order
        execute("List.viewDetail", "row=0"); // Goes to detail mode editing the first invoice
        execute("Sections.change", "activeSection=1"); // Changes to tab 1
        assertCollectionRowCount("orders", 0); // This invoice has no orders
        execute("Collection.add", // Clicks on the button for adding a new order, this takes
            "viewObject=xava_view_section1_orders"); // you to a list of orders
        execute("AddToCollection.add", "row=0"); // Chooses the first order in the list
        assertCollectionRowCount("orders", 1); // Order added to the invoice
        checkRowCollection("orders", 0); // Checks the order, to remove it
        execute("Collection.removeSelected", // Removes the recently added order
            "viewObject=xava_view_section1_orders");
        assertCollectionRowCount("orders", 0); // The order has been removed
    }
    
}
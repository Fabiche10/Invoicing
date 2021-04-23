package com.yourcompany.invoicing.model;
 
import java.math.*;
import java.time.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;

import com.yourcompany.invoicing.calculators.*;

import lombok.*;

@Entity @Getter @Setter
@View(members=
"year, number, date," + // The members for the header part in one line
"data {" + // A tab 'data' for the main data of the document
    "customer;" +
    "details;" +
    "remarks" +
"}"
)
abstract public class CommercialDocument extends Identifiable {

    @Column(length=4)
    @DefaultValueCalculator(CurrentYearCalculator.class) // Current year
    int year;
 
    @Column(length=6)
    // @DefaultValueCalculator(value=NextNumberForYearCalculator.class, // Remove this
    //      properties=@PropertyValue(name="year")
    // )
    @ReadOnly // The user cannot modify the value
    int number;
 
    @Required
    @DefaultValueCalculator(CurrentLocalDateCalculator.class) // Current date
    LocalDate date;
 
    @Stereotype("MEMO")
    String remarks;
    
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @ReferenceView("Simple") // The view named 'Simple' is used to display this reference
    Customer customer;
    
    @ElementCollection
    @ListProperties(
        "product.number, product.description, quantity, pricePerUnit, " +
        "amount+[" + 
        	"commercialDocument.vatPercentage," +
        	"commercialDocument.vat," +
        	"commercialDocument.totalAmount" +
        "]" 
    )
    private Collection<Detail> details;
 
    @OneToMany(mappedBy="invoice")
    Collection<Order> orders;
    
    @DefaultValueCalculator(VatPercentageCalculator.class)
    BigDecimal vatPercentage;
       
    @ReadOnly
    @Stereotype("MONEY")
    @Calculation("sum(details.amount) * vatPercentage / 100")
    BigDecimal vat;

    @ReadOnly
    @Stereotype("MONEY")
    @Calculation("sum(details.amount) + vat")    
    BigDecimal totalAmount;  
    
    @PrePersist // Executed just before saving the object for the first time
    private void calculateNumber() throws Exception {
        Query query = XPersistence.getManager()
            .createQuery("select max(i.number) from " +
            getClass().getSimpleName() + // Thus it's valid for both Invoice and Order
            " i where i.year = :year");
        query.setParameter("year", year);
        Integer lastNumber = (Integer) query.getSingleResult();
        this.number = lastNumber == null ? 1 : lastNumber + 1;
    }
    
    @org.hibernate.annotations.Formula("TOTALAMOUNT * 0.10") // The calculation using SQL
    @Setter(AccessLevel.NONE) // The setter is not generated, only the getter is needed
    @Stereotype("MONEY")
    BigDecimal estimatedProfit; // A field, as in the persistent property case
    
    
}
package com.design.csvprocessor.batch;


import com.design.csvprocessor.model.Visit;
import org.springframework.batch.item.ItemProcessor;


/**
 * Intermediate processor to do the operations after the reading the data from the CSV file and
 * before writing the data into SQL.
 */
public class VisitItemProcessor implements ItemProcessor<Visit, Visit>
{

    @Override
    public Visit process(final Visit visit) throws Exception
    {
        final String email = visit.getEmail().toUpperCase();
        final Visit processedVisit = new Visit(0,email, visit.getPhone(), visit.getSource());

        return processedVisit;
    }

}
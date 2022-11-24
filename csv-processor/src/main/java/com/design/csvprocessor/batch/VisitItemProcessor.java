package com.design.csvprocessor.batch;


import com.design.csvprocessor.model.Visit;
import org.springframework.batch.item.ItemProcessor;

import java.util.HashSet;
import java.util.Set;


/**
 * Intermediate processor to do the operations after the reading the data from the CSV file and
 * before writing the data into SQL.
 */
public class VisitItemProcessor implements ItemProcessor<Visit, Visit>
{
    private final Set<String> seenVisits= new HashSet<String>();

    @Override
    public Visit process(final Visit visit) throws Exception
    {
        // Data Cleaning >> nulls
        if(visit.getEmail().trim().equals("") ||
                visit.getPhone().trim().equals("") ||
                visit.getSource().trim().equals(""))
            return null;

        // Data Cleaning >> uniqueness using email+phone
        if(seenVisits.contains(visit.getEmail().trim() + visit.getPhone().trim()))
            return null;

        seenVisits.add(visit.getEmail().trim() + visit.getPhone().trim());
        return visit;
    }

}
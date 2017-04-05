package com.android.shnellers.heartrate.analysis;

import android.os.Environment;
import android.util.Log;

import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.database.diary.DiaryDatabase;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import static android.content.ContentValues.TAG;
import static com.android.shnellers.heartrate.Constants.Const.AVG;
import static com.android.shnellers.heartrate.Constants.Const.LESS_THAN_40;
import static com.android.shnellers.heartrate.Constants.Const.MAX_CAPITAL_M;
import static com.android.shnellers.heartrate.Constants.Const.MIN;
import static com.android.shnellers.heartrate.Constants.Const.OVER_100;
import static com.android.shnellers.heartrate.Constants.Const.RESTING_RATES;

/**
 * Created by Sean on 20/03/2017.
 */

public class PDFReport {

    private HeartRateDatabase mHeartRateDatabase;
    private DiaryDatabase mDiaryDatabase;

    public static File createPDFReport(int dbSize, HashMap<String, Integer> restingData) {
        Font heading = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        File pdfFolder = new File (Environment.getExternalStorageDirectory(), "pdfdemo");
        File myFile = null;
        try {


            FileOutputStream output = null;

            if (!pdfFolder.exists()) {
                pdfFolder.mkdir();
                Log.d(TAG, "PDF Directory Created");
            }

            Date date = new Date() ;
            String timeStamp = new SimpleDateFormat("yyyy/MM/dd", Locale.UK).format(date);

            myFile = new File(pdfFolder, "tempo.pdf");
            output = new FileOutputStream(myFile);

            //Step 1
            Document document = new Document();
            PdfWriter.getInstance(document, output);

            //Step 3
            document.open();

            //Step 4 Add content
            document.add(new Paragraph(new Phrase("General Overview", heading)));
            // Single line spacing
             document.add(Chunk.NEWLINE);

            // Table of general information
            document.add(createFirstTable(dbSize, restingData));
            addDoubleSpacing(document);

            // Table of post activity resting rate stats
            document.add(new Paragraph(new Phrase("Post Activity Monitoring", heading)));
            document.add(Chunk.NEWLINE);

            document.add(createActivityTableStats());

            document.add(Chunk.NEWLINE);

            String weight = "There has been little change in weight over the last";

            document.add(new Paragraph(new Phrase("Fluid Accumulation/Weight Monitoring", heading)));
            document.add(Chunk.NEWLINE);
            PdfPTable weightTable = new PdfPTable(3);
            weightTable.addCell(new Phrase("Current Weight", heading));
            weightTable.addCell(new Phrase("70kg"));
            weightTable.addCell(new Phrase(""));

            weightTable.addCell(new Phrase("Weight 3 days ago", heading));
            weightTable.addCell(new Phrase("70.2kg"));
            weightTable.addCell(new Phrase("-0.2"));

            weightTable.addCell(new Phrase("Weight 7 days ago", heading));
            weightTable.addCell(new Phrase("70.1"));
            weightTable.addCell(new Phrase("-0.1"));

            document.add(weightTable);

            document.add(Chunk.NEWLINE);
            String symptoms = "In the last 7 days the user recorded 8 symptoms of feeling unwell. These " +
                    "symptoms are recorded below.";



            String rest100 = "There were a total of 28 resting rates recorded " +
                    "which measured 100 beats per minute or more. Most of these high resting rates " +
                    "were recorded after walking/running/cycling.";


            document.add(new Paragraph(new Phrase("Symptoms Log", heading)));

            document.add(new Paragraph(new Phrase(symptoms)));
            document.add(Chunk.NEWLINE);
            PdfPTable table = new PdfPTable(2);

            table.addCell(new Phrase("3 Mar 2017"));
            table.addCell(new Phrase("Headaches"));

            table.addCell(new Phrase("3 Mar 2017"));
            table.addCell(new Phrase("Headaches"));

            table.addCell(new Phrase("3 Mar 2017"));
            table.addCell(new Phrase("Headaches"));

            document.add(table);
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(new Phrase(rest100)));
            document.add(Chunk.NEWLINE);

            document.add(createListOfHighRestingRates());



            //Step 5: Close the document
            document.close();
            output.close();


            Log.d(TAG, "downloadReport: " + myFile.getAbsolutePath());



        } catch (Exception e) {
            e.printStackTrace();
        }
        return myFile;
    }

    private static PdfPTable createListOfHighRestingRates() {
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        Font boldFontSmall = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);

        PdfPTable table = new PdfPTable(4);

        table.addCell("");
        table.addCell(new Phrase("Heart Rate", boldFontSmall));
        table.addCell(new Phrase("Time", boldFontSmall));
        table.addCell(new Phrase("Type", boldFontSmall));

        Random random = new Random();

        for (int i = 1; i <= 10; i++) {
            int n = random.nextInt(140 - 100) + 100;
            createStatsTableRow(table, new Phrase(String.valueOf(i)),
                    new Phrase(String.valueOf(n)), new Phrase("10:30"), new Phrase("Resting"));
        }

        return table;
    }

    private static PdfPTable createActivityTableStats() {
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        Font boldFontSmall = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);

        // a table with three columns
        PdfPTable table = new PdfPTable(4);


        table.addCell("");
        table.addCell(new Phrase("Post Activity Resting Rates Recorded", boldFontSmall));
        table.addCell(new Phrase("Average Resting Rate", boldFontSmall));
        table.addCell(new Phrase("Resting Rates 100+", boldFontSmall));

        createStatsTableRow(table, new Phrase("Walking", boldFont), new Phrase("187"), new Phrase("87"),new Phrase("17"));
        createStatsTableRow(table, new Phrase("Running", boldFont), new Phrase("187"), new Phrase("87"),new Phrase("17"));
        createStatsTableRow(table, new Phrase("Cycling", boldFont), new Phrase("187"), new Phrase("87"),new Phrase("17"));


        return table;
    }

    private static void createStatsTableRow(PdfPTable table, Phrase activity, Phrase postResting,
                                            Phrase average, Phrase over100) {
        table.addCell(activity);
        table.addCell(postResting);
        table.addCell(average);
        table.addCell(over100);
    }

    private static void addDoubleSpacing(Document document) {
        try {
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void downloadReport () {

    }

    /**
     * Creates our first table
     * @return our first table
     * @param dbSize
     * @param restingData
     */
    public static PdfPTable createFirstTable(int dbSize, HashMap<String, Integer> restingData) {
        // a table with three columns
        PdfPTable table = new PdfPTable(4);
        // the cell object
        PdfPCell cell;
        // we add a cell with colspan 3
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

        //
        createNewTableRow(table, new Phrase("Total Detected", boldFont), String.valueOf(dbSize));
        createNewTableRow(table, new Phrase("Resting", boldFont), String.valueOf(restingData.get(RESTING_RATES)));

        createNewTableRow(table, new Phrase("Max Resting Rate", boldFont), String.valueOf(restingData.get(MAX_CAPITAL_M)));
        createNewTableRow(table, new Phrase("Min Resting Rate", boldFont), String.valueOf(restingData.get(MIN)));

        createNewTableRow(table, new Phrase("Avg Resting Rate", boldFont), String.valueOf(restingData.get(AVG)));
        createNewTableRow(table, new Phrase("Resting rates 100+", boldFont), String.valueOf(restingData.get(OVER_100)));

        createNewTableRow(table, new Phrase("Resting Rates < 40", boldFont), String.valueOf(restingData.get(LESS_THAN_40)));
        createNewTableRow(table, new Phrase("Resting", boldFont), "9876");

        return table;
    }

    private static void createNewTableRow(PdfPTable table, Phrase heading, String value) {
        table.addCell(heading);
        table.addCell(value);
    }

    private static PdfPCell createNewRow(String cellText, boolean isHeading) {
        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 12);
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

        PdfPCell cell = new PdfPCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.addElement(new Phrase(cellText, isHeading ? boldFont : font));

        return cell;
    }


}

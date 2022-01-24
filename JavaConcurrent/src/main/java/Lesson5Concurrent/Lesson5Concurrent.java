package Lesson5Concurrent;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Lesson5Concurrent {
    
    public static long count = 0;
    
    private static Integer numberOfThreads;
    private static boolean reentrantLock = false;
    private static boolean atomicLong = false;
    

    public static void main(String[] args) throws InterruptedException, ExecutionException {
                
        parseArgs(args);
        
        String[] workSet = splitString(readFiles());

        // Use a different model based on user input
        if(reentrantLock) {
            reentrantLock(workSet);
        } else if(atomicLong) {
            atomicLong(workSet);
        } else {
            noLock(workSet);
        }

        // Print the number of characters in .java & .class files
        System.out.println("Total Character count is: " + count);
        
    }

    // Parse the Command Line Options
    private static void parseArgs(String[] args) {

        CommandLineParser parser = new DefaultParser();
        CommandLine command = null;

        // Build an options list
        Options options = new Options();

        Option n = Option.builder("n").longOpt("num-threads").desc("Number of Threads [Required]").required(true)
                .hasArg().argName("THREADS").build();

        Option r = Option.builder("r").required(false).longOpt("ReentrantLock").desc("Use Reentrant Locks").build();

        Option a = Option.builder("a").required(false).longOpt("AtomicLong").desc("Use Atomic Long").build();
        
        System.out.println(n);

        options.addOption(n);
        options.addOption(r);
        options.addOption(a);

        HelpFormatter formatter = new HelpFormatter();

        // Parse the user input
        try {
            command = parser.parse(options, args);
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
            formatter.printHelp("Lesson5Concurrent [OPTION]...", options);
            System.exit(1);
        }

        String threads = command.getOptionValue("n");

        setNumberOfThreads(Integer.parseInt(threads));

        reentrantLock = command.hasOption("r");

        atomicLong = command.hasOption("a");

        if (reentrantLock && atomicLong) {
            System.err.println("Using --ReentrantLock and --AtomicLong together is not supported.");
            formatter.printHelp("Lesson5Concurrent [OPTION]...", options);
            System.exit(1);
        }

    }
    
    // Read the files from the disk
    private static String readFiles() throws InterruptedException, ExecutionException {

        // Get a list of files in the PWD that end in .java or .class
        File[] files = (new File("C:\\Users\\Neranga65\\Devlop\\workspace\\Lesson5Concurrent\\src\\main\\java\\lesson5")).listFiles(new FilenameFilter() {
    	
    	 //File[] files = (new File(System.getProperty("user.dir"))).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.toString().endsWith(".class") || name.toString().endsWith(".java"));
            }
        });
        
      

        // Create a thread pool for file reading tasks
         // which is used for counting the characters.
        ExecutorService executor = Executors.newFixedThreadPool(files.length);

        List<Callable<String>> tasks = new ArrayList<Callable<String>>();

        // Create a thread to read each file
        for (File f : files)
            tasks.add(new ReadFile(f.toString()));

        // Start the threads
        List<Future<String>> strings = executor.invokeAll(tasks);

        // Return a String composed of all the file contents returned from the threads
        String contents = strings.stream().map(t -> {
            try {
                return t.get();
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        }).collect(Collectors.joining());

        // Stop all the threads, shutdown the ExecutorService
        executor.shutdown();
        return contents;
    }

    // Split the contents of the files into chunks for each thread
    private static String[] splitString(String s) {

        // Each element of the array will be processed by a different thread
        String[] stringArray = new String[getNumberOfThreads()];

    
        int start = 0;
        int length = (int) (s.length() / getNumberOfThreads());
        int end = length;
        int mod = s.length() % getNumberOfThreads();
        
        for (int i = 0; i < getNumberOfThreads(); i++) {

            stringArray[i] = s.substring(start, end);

            // Change the start for the next segment
            start = end;

            // Change the end of the next segment
            if (i == (getNumberOfThreads() - 2)) {
                // Add the leftover characters to the last chunk
                end = end + length + mod;
            } else {
                end = end + length;
            }
        }
        return stringArray;
    }

    private static void noLock(String[] strings) throws InterruptedException, ExecutionException {

        ExecutorService executor = Executors.newFixedThreadPool(strings.length);
        List<Callable<Long>> tasks = new ArrayList<Callable<Long>>();

        // Create a task for each chunk of work
        for (String s : strings)
            tasks.add(new CountTask(s));

        // Start all the threads
        List<Future<Long>> counts = executor.invokeAll(tasks);

        // Add all the counts from each thread to get the total
        for (Future<Long> l : counts) {
            count += l.get();
        }

        // Stop all the threads, shutdown the ExecutorService
        executor.shutdown();
    }

    private static void reentrantLock(String[] strings) throws InterruptedException, ExecutionException {

        ExecutorService executor = Executors.newFixedThreadPool(strings.length);
        List<Callable<Long>> tasks = new ArrayList<Callable<Long>>();

        // Create a task for each chunk of work
        for (String s : strings)
            tasks.add(new CountTaskWithLock(s));

        // Start all the threads
        List<Future<Long>> counts = executor.invokeAll(tasks);

        // The cumulative count is tracked inside the task as an AtomicLong
        for (Future<Long> l : counts) {
            // Get the highest value of the count in the shared counter
            if (l.get() > count)
                count = l.get();
        }

        // Stop all the threads, shutdown the ExecutorService
        executor.shutdown();
    }

    private static void atomicLong(String[] strings) throws InterruptedException, ExecutionException {

        ExecutorService executor = Executors.newFixedThreadPool(strings.length);
        List<Callable<Long>> tasks = new ArrayList<Callable<Long>>();

        // Create a task for each chunk of work
        for (String s : strings)
            tasks.add(new CountTaskWithAtomicLong(s));

        // Start all the threads
        List<Future<Long>> counts = executor.invokeAll(tasks);

        // The cumulative count is tracked inside the task as an AtomicLong
        for (Future<Long> l : counts) {
            // Get the highest value of the count in the shared counter
            if (l.get() > count)
                count = l.get();
        }

        // Stop all the threads, shutdown the ExecutorService
        executor.shutdown();
    }
    
    public static Integer getNumberOfThreads() {
        return numberOfThreads;
    }

    public static void setNumberOfThreads(Integer numberOfThreads) {
        Lesson5Concurrent.numberOfThreads = numberOfThreads;
    }

}

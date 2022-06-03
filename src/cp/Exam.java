package cp;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class Exam
{
	/**
	 * This method recursively visits a directory to find all the text files contained in it and its subdirectories.
	 * 
	 * You should consider only files ending with a .txt suffix. You are guaranteed that they will be text files.
	 * 
	 * You can assume that each text file contains a (non-empty) comma-separated sequence of
	 * (positive) numbers. For example: 100,200,34,25
	 * There won't be any new lines, spaces, etc., and the sequence never ends with a comma.
	 * 
	 * The search is recursive: if the directory contains subdirectories,
	 * these are also searched and so on so forth (until there are no more
	 * subdirectories).
	 * 
	 * This method returns a list of results. The list contains a result for each text file that you find.
	 * Each {@link Result} stores the path of its text file, and the highest number (maximum) found inside of the text file.
	 * 
	 * @param dir the directory to search
	 * @return a list of results ({@link Result}), each giving the highest number found in a file
	 */
	public static List< Result > findAll( Path dir )
	{
		List<Result> list = new ArrayList<>();
                int pro=2;
                ExecutorService executor = Executors.newFixedThreadPool( pro );
                BlockingDeque< Path > producing = new LinkedBlockingDeque<>();
                final AtomicInteger dic = new AtomicInteger(0);
                CountDownLatch producelatch = new CountDownLatch( 1 );
                producing.add(dir);
                dic.incrementAndGet();
                for(int i=0;i<pro;i++){
                executor.submit( () -> {
                    try {
                        visit( list, executor, producing,dic);
                        producelatch.countDown();
                        executor.shutdownNow();
                    } catch (InterruptedException ex) {
                        
                    }
                                
			} );
                }
            try {
                producelatch.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(Exam.class.getName()).log(Level.SEVERE, null, ex);
            }
                executor.shutdownNow();
                return(list);
                }
                
	


	/**
	 * Finds a file that contains at most (no more than) n numbers and such that all
	 * numbers in the file are equal or greater than min.
	 * 
	 * This method searches only for one (any) file in the directory
	 * (parameter dir) such that the condition above is respected.
	 * As soon as one such occurrence is found, the search can be
	 * stopped and the method can return immediately.
	 * 
	 * As for method {@code findAll}, the search is recursive.
	 */
	public static Result findAny( Path dir, int n, int min )
	{
                int con=2;
                int pro=2;
		ExecutorService executor = Executors.newFixedThreadPool( pro );
                
                CountDownLatch latch = new CountDownLatch( con );
                CountDownLatch producelatch = new CountDownLatch( 1 );
                BlockingDeque< Result > listing = new LinkedBlockingDeque<>();
                BlockingDeque< Path > producing = new LinkedBlockingDeque<>();
                Future<Result> r;
                 final AtomicInteger dic = new AtomicInteger(0);
                ExecutorService consumer = Executors.newFixedThreadPool( con);
                
                producing.add(dir);
                dic.incrementAndGet();
                Result res = null;
                for(int i=0;i<pro;i++){
                executor.submit( () -> {
                    try {
                        produce( listing, executor, producing,dic);
                        producelatch.countDown();
                        executor.shutdownNow();
                    } catch (InterruptedException ex) {
                        
                    }
                                
			} );
                }
                
                r=consumer.submit(new consume(listing,min,n,producelatch,latch));
                for(int i=0;i<con-1;i++){
                r=consumer.submit(new consume(listing,min,n,producelatch,latch));
                }
                
                
                try{
                    System.out.println("wooo");
                    try {
                        res=r.get();
                        
                    } catch (ExecutionException ex) {
                        Logger.getLogger(Exam.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }catch(InterruptedException e){}

            executor.shutdownNow();
            consumer.shutdownNow();
            return(res);
	}
	
	/**
	 * Computes overall statistics about the occurrences of numbers in a directory.
	 * 
	 * This method recursively searches the directory for all numbers in all files and returns
	 * a {@link Stats} object containing the statistics of interest. See the
	 * documentation of {@link Stats}.
	 */
	public static Stats stats( Path dir )
	{
		Stats a= new Statistic(dir);
                return(a);
	}
        
        private static void visit(List<Result> listing,ExecutorService executor,BlockingDeque< Path > producing,AtomicInteger dic) throws InterruptedException {
            while(true){
            if(dic.get()==0){
                break;
            }
            Path dir=producing.takeFirst();
            try {
                DirectoryStream<Path> stream =Files.newDirectoryStream(dir);
                for(Path subPath:stream){
                    if(executor.isShutdown()){
                        break;
                    }
                    if(Files.isDirectory(subPath)){
                        dic.getAndIncrement();
                        producing.add(subPath);
                    }else if (subPath.toString().endsWith(".txt")){
                        Result e=new TextFile(subPath);
                        synchronized(listing){
                        listing.add(e);
                        
                        }
                    }
                }
                dic.getAndDecrement();
            } catch (IOException ex) {
                Logger.getLogger(Exam.class.getName()).log(Level.SEVERE, null, ex);
            }
        }}
        
        
        private static void produce(BlockingDeque< Result > listing,ExecutorService executor,BlockingDeque< Path > producing,AtomicInteger dic) throws InterruptedException {
            while(true){
            if(dic.get()==0){
                break;
            }
            Path dir=producing.takeFirst();
            try {
                DirectoryStream<Path> stream =Files.newDirectoryStream(dir);
                for(Path subPath:stream){
                    if(executor.isShutdown()){
                        break;
                    }
                    if(Files.isDirectory(subPath)){
                        dic.getAndIncrement();
                        producing.add(subPath);
                    }else if (subPath.toString().endsWith(".txt")){
                        Result e=new TextFile(subPath);
                        listing.add(e);
                    }
                }
                dic.getAndDecrement();
            } catch (IOException ex) {
                Logger.getLogger(Exam.class.getName()).log(Level.SEVERE, null, ex);
            }
        }}
        
        




}




class consume implements Callable<Result>{
    BlockingDeque< Result > listing;
    int min;
    int n;
    CountDownLatch latch;
    CountDownLatch consumerlatch;
    
    public consume(BlockingDeque< Result > listing, int min, int n, CountDownLatch latch,CountDownLatch consumerlatch){
    this.listing=listing;
    this.min=min;
    this.n=n;
    this.latch=latch;
    this.consumerlatch=consumerlatch;
}
    public Result call(){
        Result e=null;
        long i;
        boolean keeprun=true;
        System.out.println("begin");
        while(keeprun){
            
        try {
            synchronized(latch){
               e=listing.pollFirst(100,TimeUnit.MILLISECONDS);
               i=latch.getCount();
            }
            System.out.println("almost im here");
            if(e==null&&i==0){
                System.out.println("im here");
                consumerlatch.countDown();
                consumerlatch.await();
                return(null);
            }
            BufferedReader reader;
            try {
                
                reader = Files.newBufferedReader( e.path() );
                String[] numbers = reader.readLine().split(",");
                if(textLegal(numbers,n,min)){
                        break;
                    }
            } catch (IOException ex) {
                
            }
        } catch (InterruptedException ex) {
            
        }
        }
        return(e);
    }
    
            public static boolean textLegal(String[] numbers,int n,int min){
            if(numbers.length>n){
                return(false);
            }
            for(int i=0;i<numbers.length-1;i++){
                if(Integer.parseInt(numbers[i])<min){
                    return(false);
                }
            }
            return(true);
        }
}

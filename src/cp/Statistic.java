/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cp;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sebastian
 */
public class Statistic implements Stats{
    Path path;
    
    
    
    
    Statistic(Path dir){
        this.path=dir;
    }
    
    @Override
    public int occurrences(int number) {
        ExecutorService executor = Executors.newFixedThreadPool( 2);
        ExecutorService consumer = Executors.newFixedThreadPool( 2);
        BlockingDeque< Result > listing = new LinkedBlockingDeque<>();
        BlockingDeque< Path > producing = new LinkedBlockingDeque<>();
        final AtomicInteger dic = new AtomicInteger(0);
        final AtomicInteger count = new AtomicInteger(0);
        CountDownLatch producelatch = new CountDownLatch( 1 );
        CountDownLatch consume = new CountDownLatch( 2 );
        producing.add(path);
        dic.incrementAndGet();
        System.out.println(dic.get()+"this");
        executor.submit( () -> {
            try {	
                produce(listing,executor,producing,dic);
                producelatch.countDown();
                executor.shutdownNow();
                
            } catch (InterruptedException ex) {
                
            }
			} );
        executor.submit( () -> {
            try {	
                produce(listing,executor,producing,dic);
                producelatch.countDown();
                executor.shutdownNow();
            } catch (InterruptedException ex) {
                
            }
			} );
        consumer.submit(() -> {
            try {
                OccurrenceConsume(listing,consumer,producelatch,consume,number,count);
            } catch (InterruptedException ex) {
                
            } catch (IOException ex) {
                
            }
        });

             
        try {
            consume.await();
        } catch (InterruptedException ex) {
            
        }
        consumer.shutdownNow();
        executor.shutdownNow();
        return(count.get());
    }

    @Override
    public List<Path> atMost(int max) {
        ExecutorService executor = Executors.newFixedThreadPool( 2);
        ExecutorService consumer = Executors.newFixedThreadPool( 2);
        BlockingDeque< Result > listing = new LinkedBlockingDeque<>();
        BlockingDeque< Path > producing = new LinkedBlockingDeque<>();
        final AtomicInteger dic = new AtomicInteger(0);
        CountDownLatch producelatch = new CountDownLatch( 1 );
        CountDownLatch consume = new CountDownLatch( 2 );
        producing.add(path);
        dic.incrementAndGet();
        List<Path> list=new ArrayList<>();
        executor.submit( () -> {
            try {	
                produce(listing,executor,producing,dic);
                producelatch.countDown();
                executor.shutdownNow();
            } catch (InterruptedException ex) {
                
            }});
        executor.submit( () -> {
            try {	
                produce(listing,executor,producing,dic);
                producelatch.countDown();
                executor.shutdownNow();
            } catch (InterruptedException ex) {
                
            }});
        
                consumer.submit(() -> {
            try {
                MostConsume(listing,consumer,producelatch,consume,max,list);
            } catch (InterruptedException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
            consumer.submit(() -> {
            try {
                MostConsume(listing,consumer,producelatch,consume,max,list);
            } catch (InterruptedException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        try {
            consume.await();
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
        }
        consumer.shutdownNow();
        executor.shutdownNow();
        return(list);
    }

    @Override
    public int mostFrequent() {
        ConcurrentHashMap<Integer,AtomicInteger> occur=new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool( 2);
        ExecutorService consumer = Executors.newFixedThreadPool( 2);
        BlockingDeque< Result > listing = new LinkedBlockingDeque<>();
        BlockingDeque< Path > producing = new LinkedBlockingDeque<>();
        final AtomicInteger dic = new AtomicInteger(0);
        CountDownLatch producelatch = new CountDownLatch( 1 );
        CountDownLatch consume = new CountDownLatch( 2 );
        producing.add(path);
        dic.incrementAndGet();
                executor.submit( () -> {
            try {	
                produce(listing,executor,producing,dic);
                producelatch.countDown();
                executor.shutdownNow();
                
            } catch (InterruptedException ex) {
                
            }
			} );
        executor.submit( () -> {
            try {	
                produce(listing,executor,producing,dic);
                producelatch.countDown();
                executor.shutdownNow();
            } catch (InterruptedException ex) {
                
            }
			} );
        
            consumer.submit(() -> {
            try {
                mFregConsume(listing,producelatch,consume,occur);
            } catch (InterruptedException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        consumer.submit(() -> {
            try {
                mFregConsume(listing,producelatch,consume,occur);
            } catch (InterruptedException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        try {
            consume.await();
        } catch (InterruptedException ex) {
            
        }
        executor.shutdownNow();
        consumer.shutdownNow();
        
        Map.Entry<Integer,AtomicInteger> maxValue=null;
        
        for(Map.Entry<Integer,AtomicInteger> entry:occur.entrySet()){
            if(maxValue==null||entry.getValue().get()>maxValue.getValue().get()){
                maxValue=entry;
            }
        }
        System.out.println(occur.get(10));
        return(maxValue.getKey());
    }

    @Override
    public int leastFrequent() {
        ConcurrentHashMap<Integer,AtomicInteger> occur=new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool( 2);
        ExecutorService consumer = Executors.newFixedThreadPool( 2);
        BlockingDeque< Result > listing = new LinkedBlockingDeque<>();
        BlockingDeque< Path > producing = new LinkedBlockingDeque<>();
        final AtomicInteger dic = new AtomicInteger(0);

        CountDownLatch producelatch = new CountDownLatch( 1 );
        CountDownLatch consume = new CountDownLatch( 2 );
        producing.add(path);
        dic.incrementAndGet();
                executor.submit( () -> {
            try {	
                produce(listing,executor,producing,dic);
                producelatch.countDown();
                executor.shutdownNow();
                
            } catch (InterruptedException ex) {
                
            }
			} );
        executor.submit( () -> {
            try {	
                produce(listing,executor,producing,dic);
                producelatch.countDown();
                executor.shutdownNow();
            } catch (InterruptedException ex) {
                
            }
			} );
        
            consumer.submit(() -> {
            try {
                mFregConsume(listing,producelatch,consume,occur);
            } catch (InterruptedException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        consumer.submit(() -> {
            try {
                mFregConsume(listing,producelatch,consume,occur);
            } catch (InterruptedException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        try {
            consume.await();
        } catch (InterruptedException ex) {
            
        }
        executor.shutdownNow();
        consumer.shutdownNow();
        
        Map.Entry<Integer,AtomicInteger> maxValue=null;
        
        for(Map.Entry<Integer,AtomicInteger> entry:occur.entrySet()){
            if(maxValue==null||entry.getValue().get()<maxValue.getValue().get()){
                maxValue=entry;
            }
        }
        System.out.println(occur.get(10));
        return(maxValue.getKey());
    }

    @Override
    public List<Path> byTotals() {
        TreeMap<Path,Integer> sorttotal=new TreeMap<>();
        ExecutorService executor = Executors.newFixedThreadPool( 2);
        ExecutorService consumer = Executors.newFixedThreadPool( 2);
        BlockingDeque< Result > listing = new LinkedBlockingDeque<>();
        BlockingDeque< Path > producing = new LinkedBlockingDeque<>();
        final AtomicInteger dic = new AtomicInteger(0);
        CountDownLatch producelatch = new CountDownLatch( 1 );
        CountDownLatch consume = new CountDownLatch( 1 );
        producing.add(path);
        dic.incrementAndGet();
                executor.submit( () -> {
            try {	
                produce(listing,executor,producing,dic);
                producelatch.countDown();
                executor.shutdownNow();
                
            } catch (InterruptedException ex) {
                
            }
			} );
        executor.submit( () -> {
            try {	
                produce(listing,executor,producing,dic);
                producelatch.countDown();
                executor.shutdownNow();
            } catch (InterruptedException ex) {
                
            }
			} );
            consumer.submit(() -> {
            try {
                byTotalConsume(listing,consumer,producelatch,consume,sorttotal);
            } catch (InterruptedException ex) {
                
            } catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        consumer.submit(() -> {
            try {
                byTotalConsume(listing,consumer,producelatch,consume,sorttotal);
            } catch (InterruptedException ex) {
                
            } catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
             
        try {
            consume.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
        }
        executor.shutdownNow();
        consumer.shutdownNow();
        List<Map.Entry<Path,Integer>> listy= new LinkedList<Map.Entry<Path,Integer>>(sorttotal.entrySet());
        Collections.sort(listy, new Comparator<Map.Entry<Path,Integer>>(){
            public int compare(Map.Entry<Path,Integer> o1,Map.Entry<Path,Integer> o2){
                return (o1.getValue().compareTo(o2.getValue()));
            }
        });
        List<Path> list = new ArrayList<>();
        
        for(Map.Entry<Path,Integer> key: listy.subList(0, listy.size())){
            System.out.println(key);
            list.add(key.getKey());
        }
        System.out.println("done");
        for(int i=0;i<list.size();i++){
            System.out.println(list.get(i));
        }
        return(list);
    }
    
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
       
        
    private static void OccurrenceConsume(BlockingDeque< Result > listing,ExecutorService executor, CountDownLatch latch,CountDownLatch consumerlatch, int number,AtomicInteger count) throws InterruptedException, IOException{
        boolean keeprun=true;
        Result e=null;
        long i;
        while(keeprun){
            synchronized(latch){
                e=listing.poll(100,TimeUnit.MILLISECONDS);
                i=latch.getCount();
                
            }
            if(e==null&&i==0){
                System.out.println("im here");
                consumerlatch.countDown();
                consumerlatch.await();
                break;
            } else{
            BufferedReader reader;

                
            try {
                reader = Files.newBufferedReader( e.path() );
                String[] numbers = reader.readLine().split(",");
                for(String num: numbers){
                if (number==Integer.parseInt(num)){
                    count.getAndIncrement();
            }}} catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }}

                }    

        }
    
    
    private static void MostConsume(BlockingDeque< Result > listing,ExecutorService executor, CountDownLatch latch,CountDownLatch consumerlatch, int number, List<Path> list) throws InterruptedException, IOException{
        boolean keeprun=true;
        Result e=null;
        long i;
        while(keeprun){
            synchronized(latch){
                e=listing.poll(100,TimeUnit.MILLISECONDS);
                i=latch.getCount();
                
            }
            if(e==null&&i==0){
                System.out.println("im here");
                consumerlatch.countDown();
                consumerlatch.await();
                break;
            } else{
            BufferedReader reader;

                
            try {
                reader = Files.newBufferedReader( e.path() );
                String[] numbers = reader.readLine().split(",");
                int b=0;
                for(String num: numbers){
                    if (b<Integer.parseInt(num)){
                    b=Integer.parseInt(num);
                    }
            } if (b<=number){
                synchronized(list){
                list.add(e.path());
                }
            }
            } catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }}

                }    

        }
    
     private static void mFregConsume(BlockingDeque< Result > listing, CountDownLatch latch,CountDownLatch consumerlatch,ConcurrentHashMap<Integer,AtomicInteger> times) throws InterruptedException, IOException{
        boolean keeprun=true;
        Result e=null;
        long i;
        while(keeprun){
            synchronized(latch){
                e=listing.poll(100,TimeUnit.MILLISECONDS);
                i=latch.getCount();
                
            }
            if(e==null&&i==0){
                System.out.println("im here");
                consumerlatch.countDown();
                consumerlatch.await();
                break;
            } else{
            BufferedReader reader;

                
            try {
                reader = Files.newBufferedReader( e.path() );
                String[] numbers = reader.readLine().split(",");
                for(String num: numbers){
                times.computeIfAbsent(Integer.parseInt(num), k->new AtomicInteger()).getAndIncrement();
            }
            } catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }}

                }    

        }
        
        private static void byTotalConsume(BlockingDeque< Result > listing,ExecutorService executor, CountDownLatch latch,CountDownLatch consumerlatch, TreeMap<Path,Integer> map) throws InterruptedException, IOException{
        Result e=null;
        long i;
        while(true){
            synchronized(latch){
                e=listing.poll(100,TimeUnit.MILLISECONDS);
                i=latch.getCount();
            }
            if(e==null&&i==0){
                consumerlatch.countDown();
                consumerlatch.await();
                break;
            } else{
            BufferedReader reader;  
            try {
                reader = Files.newBufferedReader( e.path() );
                String[] numbers = reader.readLine().split(",");
                int b=0;
                for(String num: numbers){
                b=b+Integer.parseInt(num);
            }
                synchronized(map){
                map.put(e.path(),b );
                }
            } catch (IOException ex) {
                Logger.getLogger(Statistic.class.getName()).log(Level.SEVERE, null, ex);
            }}

                }    

        }
        }
 
        
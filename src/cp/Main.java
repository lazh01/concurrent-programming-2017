package cp;



/**
 * This class is present only for helping you in testing your software.
 * It will be completely ignored in the evaluation.
 * 
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main
{
	public static void main( String[] args )
	{
            Exam a= new Exam();
            Path dir=Paths.get("\\Users\\sebastian\\Dropbox\\cp2017-master\\exam");
            
            List<Result> u=Exam.findAll(dir);
            System.out.println(u.size());
            for(int i=0;i<u.size();i++){
                System.out.println(u.get(i).path());
            }
//            Result e=Exam.findAny(dir, 1000, 5);
//            System.out.println(e.number()+" "+e.path());

//            Stats s=Exam.stats(dir);
//            System.out.println(s.atMost(10000000).get(5));
//            System.out.println(s.occurrences(10));
//            System.out.println(s.mostFrequent());
//            System.out.println(s.leastFrequent());
//            s.byTotals();
	}
}

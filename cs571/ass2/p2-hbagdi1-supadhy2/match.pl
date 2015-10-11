
#Shashi Upadhyay BUID: B00627613
#Hardik Bagdi    BUID: B00576043


my $filename = "input.txt";

if(-e $filename && -r $filename){
    
    open FILE, "$filename" or die $!;
    my $word;
    print("\n");
    while(<FILE>){
        $word = "$_";
        chomp($word);
        # string that contains hi
        if($word=~m/hi/){
            print "$word contains \"hi\" \n";
        }
        
        # string that contains exactly one vowel characters
        if($word=~m/^[^aeiou]*[aeiou]{1}[^aeiou]*$/){
            print "$word contains exactly one vowel characters \n";
        }
        
        # string that contains two or more 'l'
	$count= $word=~tr/l/l/;
        if($count>=2){
            print "$word contains two or more \'l\' \n";
        }
        
        # string that begin with letter "h" and end with "t"
        if($word=~/^h/ && $word=~/t$/){
            print "$word begin with letter h and ends with t \n";
        }
        print("\n\n");
        
    }#end of while loop
    
    close(FILE);
    
}else{
    print "$filename : file does not exist\n";
}

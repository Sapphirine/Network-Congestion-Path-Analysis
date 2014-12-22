#!/usr/bin/perl


use warnings;
use strict;

my $input_file = "";
my $output_file = "";
my @line_parse_array;
my @lines;
my $path_cost;
my @sources;
my @destinations;

foreach (@ARGV){
   if(m/-f=/){
       $input_file = $_;
       $input_file =~ s/-f=//g;
   }
   if(m/-o=/){
       $output_file = $_;
       $output_file =~ s/-o=//g;
   }
   if(m/-h/){
      help();
   }


}

#Print out the input file name and output file name
chomp($input_file);
if($input_file ne ""){
   print "Input file $input_file\n";
}
else{
   print "No Input Specified!!\n";
   help();
}
chomp($output_file);
if($output_file ne ""){
   print "Outpit file $output_file\n";
}
else{
   print "No Output Specified!!\n";
   help();
}

#Open the input file
open FILE, "<" . $input_file or die $!;
@lines = <FILE>;
close(FILE);
#Open the output file
open(FILE, ">$output_file") or die "Cannot open file";


print "Parsing $input_file and loading $output_file as csv\n";


#Parse the file
for (@lines) {
   if($_ =~ /#/){
      #print "Disregarded line\n";
   }
   else{
      @line_parse_array = split('\t',$_);
      if($line_parse_array[0] =~ /M/){
         #This is a monitor, we don't need this data
      }
      if(($line_parse_array[0] =~ /D/) || ($line_parse_array[0] =~ /I/)){
         if($line_parse_array[0] =~ /D/){
            #This is a direct link, the path cost here is 1 hop (A TO B) and is directional
            $path_cost = 1;
         }
         else{
            #This is an idirect link, the path cost here is 1 + the gap (array value 3) hops (A TO B) and is directional
            $path_cost = 1 + $line_parse_array[3];
         }
         if(($line_parse_array[1] =~/_/) || ($line_parse_array[1] =~/,/)){
            #This is a multi-origin AS, so generate an entry for each AS in the source to the destination
            @sources = split(/[,_]/,$line_parse_array[1]);
         }
         else{
            @sources = $line_parse_array[1];
         }
         if(($line_parse_array[2] =~/_/) || ($line_parse_array[2] =~/,/)){
            #This is a multi-origin AS, so generate an entry for each AS in the source to the destination
            @destinations = split(/[,_]/,$line_parse_array[2]);
         }
         else{
            @destinations = $line_parse_array[2];
         }
         foreach my $source (@sources){
            foreach my $destination (@destinations){
               printf FILE ("%s,%s,%d\n",$source,$destination,$path_cost);
            }#for each dest
         }#for each source
      }#End if D or I
   }#End Valid Line
}#End for each line

#Close output file
close(FILE);

sub help {
   print "Help Screen\n";
   print "-f=<file_name>   :File to parse\n";
   print "-o=<file_name>   :Output CSV File\n";
   print "-h               :Print this help menu\n";
   exit;
}

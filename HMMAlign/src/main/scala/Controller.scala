package main.scala

import scala.collection.JavaConversions._
import scala.io._
import java.io._

import scala.collection.mutable._
import scala.sys.process._
import java.util.zip._

import main.scala.dp.ConvexDP
import main.scala.fasta.Fasta

import scala.util.Random

/**
  * created by aaronmck on 2/13/14
  *
  * Copyright (c) 2014, aaronmck
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  * 2.  Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
  *
  */



object Controller extends App {
  val NOTAREALFILENAME = "/0192348102jr10234712930h8j19p0hjf129-348h512935"
  // please don't make a file with this name
  val NOTAREALFILE = new File(NOTAREALFILENAME)


  // parse the command line arguments
  val parser = new scopt.OptionParser[AlignmentConfig]("UMIMerge") {
    head("Alignment", "1.1")

    // *********************************** Inputs *******************************************************
    opt[File]("inputReads") required() valueName ("<file>") action { (x, c) => c.copy(input = x) } text ("the input file to align")
    opt[File]("reference") required() valueName ("<file>") action { (x, c) => c.copy(fasta = x) } text ("the fasta reference")
    opt[File]("outputReads") required() valueName ("<file>") action { (x, c) => c.copy(output = x) } text ("the the output alignment, in FASTA format")
    opt[Double]("matchSchore") valueName ("<Double>") action { (x, c) => c.copy(matchScore = x) } text ("how much a match is worth (default 3)")
    opt[Double]("mismatchCost") valueName ("<Double>") action { (x, c) => c.copy(mismatchScore = x) } text ("how much a mimatch costs (default 4, this is a cost, so inverted)")
    opt[Double]("gapOpenCost") valueName ("<Double>") action { (x, c) => c.copy(gapOpenCost = x) } text ("how much a gap open costs (default 10, this is a cost, so inverted)")
    opt[Double]("gapExtensionCost") valueName ("<Double>") action { (x, c) => c.copy(gapExtensionCost = x) } text ("how much a gap extension costs (default 1, this is a cost, so inverted)")

    // some general command-line setup stuff
    note("align a set of reads to a reference sequence\n")
    help("help") text ("prints the usage information you see here")
  }


  // *********************************** Run *******************************************************
  parser.parse(args, AlignmentConfig()) map { config => {


    // read in the reference file
    val ref = new Fasta(config.fasta.getAbsolutePath).readBuffer(0)

    val reads = new Fastq(config.input.getAbsolutePath)

    val output = new PrintWriter(config.output)

    reads.readBuffer.zipWithIndex.foreach { case (read, index) => {
      val nmw = new ConvexDP(read.sequence, ref.sequence, config.matchScore, -1.0 * config.mismatchScore, config.gapOpenCost, config.gapExtensionCost)
      val align = nmw.alignment

      output.write(">" + ref.name + "\n" + align.getAlignmentString._2 + "\n")
      output.write(">" + read.name + "\n" + align.getAlignmentString._1 + "\n")

      if ((index + 1) % 20 == 0) println("count " + (index + 1))
    }
    }

    output.close()

  }

  } getOrElse {
    println("Unable to parse the command line arguments you passed in, please check that your parameters are correct")
  }

}

case class AlignmentConfig(input: File = new File(Controller.NOTAREALFILENAME),
                           fasta: File = new File(Controller.NOTAREALFILENAME),
                           output: File = new File(Controller.NOTAREALFILENAME),
                           matchScore: Double = 3.0,
                           mismatchScore: Double = -4.0,
                           gapOpenCost: Double = 10.0,
                           gapExtensionCost: Double = 1.0)
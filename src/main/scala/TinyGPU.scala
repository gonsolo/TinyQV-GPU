// Copyright Andreas Wendleder 2025
// CERN-OHL-S-2.0
  
package tinygpu

import chisel3._
import circt.stage.ChiselStage

class TinyGPU extends Module {
  val io = IO(new Bundle {
    val clk = Input(Clock())
  })
}

object Main extends App {
  println(
    ChiselStage.emitSystemVerilogFile(
      gen = new TinyGPU(),
      args = Array("--target-dir", "output")
    )
  )
}

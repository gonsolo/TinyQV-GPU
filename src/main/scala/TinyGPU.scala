// Copyright Andreas Wendleder 2025
// CERN-OHL-S-2.0
  
package tinygpu

import chisel3._
import chisel3.util.{MuxCase, Cat}
import circt.stage.ChiselStage

class TinyGPU extends Module {
  val io = IO(new Bundle {
    val ui_in = Input(UInt(8.W))
    val uo_out = Output(UInt(8.W))
    val address = Input(UInt(6.W))
    val data_in = Input(UInt(32.W))
    val data_write_n = Input(UInt(2.W))
    val data_read_n = Input(UInt(2.W))
    val data_out = Output(UInt(32.W))
    val data_ready = Output(Bool())
    val user_interrupt = Output(Bool())
  })

  val example_data = RegInit(0.U(32.W))

  // Refactored write logic to use a MuxCase, which is the idiomatic
  // way to handle mutually exclusive assignments and prevent
  // multi-driver errors in Chisel.
  val next_example_data = MuxCase(example_data, Seq(
    // 32-bit write, highest priority
    (io.address === "h0".U && io.data_write_n === "b10".U) -> io.data_in,
    // 16-bit write
    (io.address === "h0".U && io.data_write_n === "b01".U) -> Cat(example_data(31, 16), io.data_in(15, 0)),
    // 8-bit write
    (io.address === "h0".U && io.data_write_n === "b00".U) -> Cat(example_data(31, 8), io.data_in(7, 0))
  ))

  // Assign the next state to the register on the clock edge.
  example_data := next_example_data

  io.uo_out := example_data(7, 0) + io.ui_in

  // Address 0 reads the example data register.
  // Address 4 reads ui_in.
  // All other addresses read 0.
  io.data_out := MuxCase(0.U, Seq(
    (io.address === "h0".U) -> example_data,
    (io.address === "h4".U) -> io.ui_in.pad(32)
  ))

  // All reads complete in 1 clock
  io.data_ready := true.B

  // User interrupt is generated on rising edge of ui_in[6], and cleared by writing a 1 to the low bit of address 8.
  val example_interrupt = RegInit(false.B)
  val last_ui_in_6 = RegInit(false.B)

  when(io.ui_in(6) && !last_ui_in_6) {
    example_interrupt := true.B
  }.elsewhen(io.address === "h8".U && io.data_write_n =/= "b11".U && io.data_in(0) === 1.U) {
    example_interrupt := false.B
  }

  last_ui_in_6 := io.ui_in(6)

  io.user_interrupt := example_interrupt
}

object Main extends App {
  println(
    ChiselStage.emitSystemVerilogFile(
      gen = new TinyGPU(),
      args = Array("--target-dir", "output")
    )
  )
}

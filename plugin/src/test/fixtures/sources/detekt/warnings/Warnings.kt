class Warnings {

    private fun foo() {
        for (i in 1..2) {
            break
            println() // unreachable
        }
    }
}
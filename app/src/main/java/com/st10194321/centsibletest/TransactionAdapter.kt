import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.st10194321.centsibletest.R
import com.st10194321.centsibletest.Transaction
import com.st10194321.centsibletest.databinding.ItemTransactionBinding

//class TransactionAdapter(
//    private val items: List<Transaction>,
//    private val onClick: (Transaction) -> Unit
//) : RecyclerView.Adapter<TransactionAdapter.VH>() {
//
//    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
//        private val name = view.findViewById<TextView>(R.id.tvTransactionName)
//        private val amount = view.findViewById<TextView>(R.id.tvTransactionAmount)
//        private val date = view.findViewById<TextView>(R.id.tvTransactionDate)
//
//        fun bind(tx: Transaction) {
//            name.text = tx.name
//            amount.text = "R %.2f".format(tx.amount)
//            date.text = tx.date
//            itemView.setOnClickListener { onClick(tx) }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.transaction_item, parent, false)
//        return VH(view)
//    }
//
//    override fun onBindViewHolder(holder: VH, position: Int) {
//        holder.bind(items[position])
//    }
//
//    override fun getItemCount() = items.size
//}

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onTransactionClick: (Transaction) -> Unit
   // val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)

) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvTransactionName.text = transaction.name
            binding.tvTransactionAmount.text = "R ${transaction.amount}"
            binding.tvTransactionDate.text = transaction.date

            itemView.setOnClickListener {
                onTransactionClick(transaction)
            }
        }
    }
}

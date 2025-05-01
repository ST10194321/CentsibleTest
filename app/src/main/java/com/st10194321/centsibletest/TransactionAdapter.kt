import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.st10194321.centsibletest.Transaction
import com.st10194321.centsibletest.databinding.ItemTransactionBinding

//Author: Rakeshdash
//Accessibilty: https://stackoverflow.com/questions/43012903/trying-to-create-a-simple-recyclerview-in-kotlin-but-the-adapter-is-not-applyin
//Date Accessed: 28/04/2025


class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onTransactionClick: (Transaction) -> Unit

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

            //bonds the variables
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

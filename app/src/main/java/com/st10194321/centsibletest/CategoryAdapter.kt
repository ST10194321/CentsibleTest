import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.st10194321.centsibletest.Category
import com.st10194321.centsibletest.R

class CategoryAdapter(
    private val items: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {


    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val icon    = view.findViewById<ImageView>(R.id.ivIcon)
        private val name    = view.findViewById<TextView>(R.id.tvName)

        fun bind(cat: Category) {
            name.text = cat.name
            icon.setImageResource(
                when (cat.name.lowercase()) {
                    "car"     -> R.drawable.car_front_2
                    "medical" -> R.drawable.medical
                    "food"    -> R.drawable.food
                    "savings" -> R.drawable.savings
                    else      -> R.drawable.category
                }
            )
            itemView.setOnClickListener { onClick(cat) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}

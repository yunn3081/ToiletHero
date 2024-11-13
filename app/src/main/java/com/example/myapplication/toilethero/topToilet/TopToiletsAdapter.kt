import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

data class Toilet(
    val roomID: String = "",
    val roomNumber: String = "",
    val buildingName: String = "",
    val averageOverallScore: Float = 0f,
    val city: String = "",
    val floorNumber: Int = 0,
    val gpsCoordinates: String = "",
    val street: String = ""
)

class ToiletAdapter(
    private val toilets: List<Toilet>,
    private val onItemClicked: (String) -> Unit
) : RecyclerView.Adapter<ToiletAdapter.ToiletViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToiletViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_toilet, parent, false)
        return ToiletViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToiletViewHolder, position: Int) {
        val toilet = toilets[position]
        holder.bind(toilet)

        // response on click
        holder.itemView.setOnClickListener {
            onItemClicked(toilet.roomID)
        }
    }

    override fun getItemCount(): Int = toilets.size

    inner class ToiletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val toiletName: TextView = itemView.findViewById(R.id.toiletName)
        private val toiletScore: TextView = itemView.findViewById(R.id.toiletScore)

        fun bind(toilet: Toilet) {
            // set toiletName as roomNumber + buildingName
            toiletName.text = "${toilet.roomNumber} - ${toilet.buildingName}"
            // set toiletScore as averageOverallScore
            toiletScore.text = "Rating: ${toilet.averageOverallScore}"
        }
    }
}

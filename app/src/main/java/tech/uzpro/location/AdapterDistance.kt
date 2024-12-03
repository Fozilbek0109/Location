package tech.uzpro.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tech.uzpro.location.databinding.ItemDistanceBinding

class AdapterDistance(
    private val list: List<String>
): RecyclerView.Adapter<AdapterDistance.VH>() {

    inner class VH(val binding:ItemDistanceBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDistanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int= list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.distance.text = list[position]
    }


}
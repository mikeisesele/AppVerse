package com.michael.appverse.features.locationTracker.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.michael.appverse.R
import com.michael.appverse.features.locationTracker.model.Run
import com.michael.appverse.features.locationTracker.utils.TrackingUtils
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_run,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]

        holder.itemView.apply {
            holder.itemView.findViewById<ImageView>(R.id.ivRunImage)?.let { Glide.with(this).load(run.image).into(it) }
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp // date in milliseconds
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            holder.itemView.findViewById<TextView>(R.id.tvDate).text = dateFormat.format(calendar.time)

            val avgSpeed = "${run.avgSpeed}km/h"
            holder.itemView.findViewById<TextView>(R.id.tvAvgSpeed).text = avgSpeed

            val distanceInKm = "${run.distanceInMeters / 1000f}km"
            holder.itemView.findViewById<TextView>(R.id.tvDistance).text = distanceInKm

            holder.itemView.findViewById<TextView>(R.id.tvTime).text = TrackingUtils.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned}kcal"
            holder.itemView.findViewById<TextView>(R.id.tvCalories).text = caloriesBurned
        }
    }
}
import './collectionCard.css'

const CollectionCard = ({title, place, prevs}) => {
    return (
        <div className='collection-card'>
            <h2 className="collection-title">
                    {title} — <span className="collection-place">{place}</span>
            </h2>
            <div className='collection-prevs'>
                {
                    prevs ?
                    prevs.map(prev => <img key={prev} className='collection-prev' src={prev} />) :
                    <p className='collection-noprevs'>No images found</p>
                }
            </div>
        </div>
    )
}

export default CollectionCard;